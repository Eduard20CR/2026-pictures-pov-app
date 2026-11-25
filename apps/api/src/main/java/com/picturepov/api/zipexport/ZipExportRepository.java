package com.picturepov.api.zipexport;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

interface ZipExportRepository extends JpaRepository<ZipExport, UUID> {

    Optional<ZipExport> findFirstByEventIdOrderByCreatedAtDesc(UUID eventId);

    /** Whether the event has a processed photo: only those go into the ZIP (apps/zip-export). */
    @Query("""
            SELECT COUNT(i) > 0 FROM Image i
            WHERE i.eventId = :eventId AND i.status = com.picturepov.api.image.ImageStatus.READY
            """)
    boolean hasProcessedPhotos(UUID eventId);

    /** A ZIP the worker will never receive: shown as failed at once, so it can be requested again. */
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("""
            UPDATE ZipExport e
            SET e.status = com.picturepov.api.zipexport.ZipExportStatus.FAILED,
                e.errorMessage = :reason, e.completedAt = CURRENT_TIMESTAMP
            WHERE e.id = :id AND e.status = com.picturepov.api.zipexport.ZipExportStatus.PENDING
            """)
    void markNotQueued(UUID id, String reason);
}
