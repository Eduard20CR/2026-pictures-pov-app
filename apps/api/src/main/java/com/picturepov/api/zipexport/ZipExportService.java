package com.picturepov.api.zipexport;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.event.EventAccess;
import com.picturepov.api.image.PhotoLinkSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Requesting the event's ZIP and following it (RF-ORG-14, ADR 0008). */
@Service
class ZipExportService {

    private final EventAccess eventAccess;
    private final ZipExportRepository zipExportRepository;
    private final ZipExportQueue queue;
    private final JdbcClient jdbc;
    private final TransactionTemplate transaction;
    private final Duration maxRun;
    private final PhotoLinkSigner photoLinkSigner;
    private final Duration downloadUrlTtl;

    ZipExportService(EventAccess eventAccess,
                     ZipExportRepository zipExportRepository,
                     ZipExportQueue queue,
                     JdbcClient jdbc,
                     PlatformTransactionManager transactionManager,
                     @Value("${app.zips.max-run}") Duration maxRun,
                     PhotoLinkSigner photoLinkSigner,
                     @Value("${app.zips.download-url-ttl}") Duration downloadUrlTtl) {
        this.eventAccess = eventAccess;
        this.zipExportRepository = zipExportRepository;
        this.queue = queue;
        this.jdbc = jdbc;
        this.transaction = new TransactionTemplate(transactionManager);
        this.maxRun = maxRun;
        this.photoLinkSigner = photoLinkSigner;
        this.downloadUrlTtl = downloadUrlTtl;
    }

    /** Queues a new ZIP, or returns the one in progress; 409 if the event has no processed photo. Queued after the commit, so the worker always finds the row. */
    ZipExportResponse request(UUID eventId, UUID userId) {
        Requested requested = transaction.execute(status -> {
            eventAccess.requireOrganizer(eventId, userId);
            lockEvent(eventId);
            Optional<ZipExport> latest = zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId);
            if (latest.isPresent() && latest.get().isInProgress(Instant.now(), maxRun)) {
                return new Requested(latest.get(), false);
            }
            if (!zipExportRepository.hasProcessedPhotos(eventId)) {
                throw new ConflictException(ErrorCode.NO_PHOTOS_TO_EXPORT, "The event has no photos to export yet");
            }
            return new Requested(zipExportRepository.saveAndFlush(ZipExport.createPending(eventId, userId)), true);
        });
        if (requested.isNew()) {
            send(requested.export().getId());
        }
        return toResponse(requested.export());
    }

    private void send(UUID exportId) {
        try {
            queue.send(exportId);
        } catch (RuntimeException e) {
            transaction.executeWithoutResult(status -> zipExportRepository.markNotQueued(exportId, reason(e)));
            throw e;
        }
    }

    /** Fits zip_exports.error_message. */
    private static String reason(RuntimeException e) {
        String reason = "Not queued: " + e.getMessage();
        return reason.length() > 1000 ? reason.substring(0, 1000) : reason;
    }

    @Transactional(readOnly = true)
    Optional<ZipExportResponse> latest(UUID eventId, UUID userId) {
        eventAccess.requireOrganizer(eventId, userId);
        return zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId).map(this::toResponse);
    }

    /** Signs a fresh link on every read, so the organizer always gets a full P-07. */
    private ZipExportResponse toResponse(ZipExport export) {
        if (export.isAvailable()) {
            String fileName = export.getId() + ".zip";
            return new ZipExportResponse(ZipExportResponse.State.AVAILABLE, export.getCreatedAt(),
                    export.getImageCount(), export.getSizeBytes(),
                    photoLinkSigner.downloadLink(export.getS3Key(), fileName, downloadUrlTtl));
        }
        ZipExportResponse.State state = export.isInProgress(Instant.now(), maxRun)
                ? ZipExportResponse.State.IN_PROGRESS
                : ZipExportResponse.State.FAILED;
        return new ZipExportResponse(state, export.getCreatedAt(), null, null, null);
    }

    /** One request per event at a time, so two concurrent requests never start two generations. */
    private void lockEvent(UUID eventId) {
        jdbc.sql("SELECT id FROM events WHERE id = :id FOR UPDATE").param("id", eventId).query(UUID.class).single();
    }

    private record Requested(ZipExport export, boolean isNew) {}
}
