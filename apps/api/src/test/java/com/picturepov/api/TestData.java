package com.picturepov.api;

import com.picturepov.api.invitation.InvitationRole;
import com.picturepov.api.invitation.InvitationStatus;
import org.springframework.jdbc.core.simple.JdbcClient;

import java.util.UUID;

/** Rows for repository tests, written with plain SQL so each test states exactly what the database holds. */
public final class TestData {

    private final JdbcClient jdbc;

    public TestData(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public UUID event() {
        return event("Boda Ana y Luis", "2099-12-12");
    }

    public UUID event(String name, String eventDate) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO events (id, name, color, public_code, event_date, images_per_user, status)
                VALUES (:id, :name, '#000000', :code, CAST(:eventDate AS DATE), 10, 'OPEN')
                """)
                .param("id", id).param("name", name).param("eventDate", eventDate)
                .param("code", publicCode(id))
                .update();
        return id;
    }

    public static String publicCode(UUID eventId) {
        return eventId.toString().substring(0, 22);
    }

    public void deleteEvent(UUID eventId) {
        jdbc.sql("UPDATE events SET deleted_at = now() WHERE id = :id").param("id", eventId).update();
    }

    public UUID user(String email) {
        UUID id = UUID.randomUUID();
        jdbc.sql("INSERT INTO users (id, email) VALUES (:id, :email)")
                .param("id", id).param("email", email).update();
        return id;
    }

    /** An invitation for the user's email, linked to the user. */
    public UUID invite(UUID eventId, UUID userId, InvitationRole role, InvitationStatus status) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO invitations (id, event_id, email, user_id, role, status)
                SELECT :id, :eventId, email, id, :role, :status FROM users WHERE id = :userId
                """)
                .param("id", id).param("eventId", eventId).param("userId", userId)
                .param("role", role.name()).param("status", status.name())
                .update();
        return id;
    }

    /** A Pending MODERATOR invitation for an email with no account yet. */
    public UUID pendingInvitation(UUID eventId, String email) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO invitations (id, event_id, email, role, status)
                VALUES (:id, :eventId, :email, 'MODERATOR', 'PENDING')
                """)
                .param("id", id).param("eventId", eventId).param("email", email)
                .update();
        return id;
    }

    /** A JPEG photo uploaded at createdAt (ISO instant); status READY or PENDING. */
    public UUID photo(UUID eventId, String createdAt, String status) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO images (id, event_id, s3_key, thumb_key, content_type, size_bytes, guest_id, status, created_at)
                VALUES (:id, :eventId, :s3Key, :thumbKey, 'image/jpeg', 1000, :guestId, :status, CAST(:createdAt AS timestamptz))
                """)
                .param("id", id).param("eventId", eventId)
                .param("s3Key", "originals/" + eventId + "/" + id)
                .param("thumbKey", "READY".equals(status) ? "thumbs/" + eventId + "/" + id + ".webp" : null)
                .param("guestId", UUID.randomUUID())
                .param("status", status)
                .param("createdAt", createdAt)
                .update();
        return id;
    }

    /** A ZIP of the event requested by the user at createdAt (ISO instant). */
    public UUID zipExport(UUID eventId, UUID userId, String status, String createdAt) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO zip_exports (id, event_id, requested_by, status, created_at)
                VALUES (:id, :eventId, :userId, :status, CAST(:createdAt AS timestamptz))
                """)
                .param("id", id).param("eventId", eventId).param("userId", userId)
                .param("status", status).param("createdAt", createdAt)
                .update();
        return id;
    }
}
