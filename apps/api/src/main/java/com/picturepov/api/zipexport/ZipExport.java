package com.picturepov.api.zipexport;

import jakarta.persistence.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

/** One ZIP of an event's photos; the worker fills in the rest (apps/zip-export). */
@Entity
@Table(name = "zip_exports")
class ZipExport {

    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "requested_by")
    private UUID requestedBy;

    @Column(name = "s3_key")
    private String s3Key;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ZipExportStatus status;

    @Column(name = "image_count")
    private Integer imageCount;

    @Column(name = "size_bytes")
    private Long sizeBytes;

    @Column(name = "error_message")
    private String errorMessage;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected ZipExport() {}

    static ZipExport createPending(UUID eventId, UUID requestedBy) {
        ZipExport export = new ZipExport();
        export.id = UUID.randomUUID();
        export.eventId = eventId;
        export.requestedBy = requestedBy;
        export.status = ZipExportStatus.PENDING;
        export.createdAt = Instant.now();
        return export;
    }

    /**
     * Queued or running: a new request must not start another generation.
     * Past maxRun (every SQS receive of the worker) it is failed: a timeout or crash on the last receive leaves it unmarked.
     */
    boolean isInProgress(Instant now, Duration maxRun) {
        return (status == ZipExportStatus.PENDING || status == ZipExportStatus.RUNNING)
                && now.isBefore(createdAt.plus(maxRun));
    }

    /** The worker stored the ZIP. */
    boolean isAvailable() {
        return status == ZipExportStatus.READY;
    }

    UUID getId() { return id; }
    String getS3Key() { return s3Key; }
    ZipExportStatus getStatus() { return status; }
    Integer getImageCount() { return imageCount; }
    Long getSizeBytes() { return sizeBytes; }
    Instant getCreatedAt() { return createdAt; }
    Instant getCompletedAt() { return completedAt; }
}
