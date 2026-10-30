package com.picturepov.api.guest;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
class GuestLimitRepository {
    private final JdbcClient jdbc;

    GuestLimitRepository(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    boolean reserveUpload(UUID eventId, UUID guestId, int limit) {
        int rows = jdbc.sql("""
                INSERT INTO guest_limits (id, event_id, guest_id, upload_count)
                VALUES (gen_random_uuid(), :eventId, :guestId, 1)
                ON CONFLICT (event_id, guest_id)
                DO UPDATE SET upload_count = guest_limits.upload_count + 1,
                              updated_at   = now()
                WHERE guest_limits.upload_count < :limit
                """)
                .param("eventId", eventId)
                .param("guestId", guestId)
                .param("limit", limit)
                .update();
        return rows == 1;
    }

    int countUploads(UUID eventId, UUID guestId) {
        return jdbc.sql("""
                SELECT upload_count
                FROM guest_limits
                WHERE event_id = :eventId AND guest_id = :guestId
                """)
                .param("eventId", eventId)
                .param("guestId", guestId)
                .query(Integer.class)
                .optional()
                .orElse(0);
    }
}
