package com.picturepov.api.image;


import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface ImageRepository extends JpaRepository<Image, UUID> {

    Optional<Image> findByIdAndEventIdAndStatus(UUID id, UUID eventId, ImageStatus status);

    List<Image> findByEventIdOrderByCreatedAtDescIdDesc(UUID eventId, Limit limit);

    /** The photos after the cursor (createdAt, id), in the same order. */
    @Query("""
            SELECT i FROM Image i
            WHERE i.eventId = :eventId
              AND (i.createdAt < :createdAt OR (i.createdAt = :createdAt AND i.id < :id))
            ORDER BY i.createdAt DESC, i.id DESC
            """)
    List<Image> findPageAfter(UUID eventId, Instant createdAt, UUID id, Limit limit);
}
