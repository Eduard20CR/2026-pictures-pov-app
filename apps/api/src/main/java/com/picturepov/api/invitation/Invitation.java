package com.picturepov.api.invitation;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

@Entity
@Table(name = "invitations")
public class Invitation {

    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "email")
    private String email;

    @Column(name = "user_id")
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private InvitationRole role;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private InvitationStatus status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    protected Invitation() {}

    public static Invitation createOwner(UUID eventId, String email) {
        return createPending(eventId, email, InvitationRole.OWNER);
    }

    public static Invitation createModerator(UUID eventId, String email) {
        return createPending(eventId, email, InvitationRole.MODERATOR);
    }

    /** Emails are stored in lowercase: one invitation per event and email. */
    public static String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static Invitation createPending(UUID eventId, String email, InvitationRole role) {
        Invitation invitation = new Invitation();
        invitation.id = UUID.randomUUID();
        invitation.eventId = eventId;
        invitation.email = normalizeEmail(email);
        invitation.role = role;
        invitation.status = InvitationStatus.PENDING;
        return invitation;
    }

    public UUID getId() { return id; }
    public UUID getEventId() { return eventId; }
    public String getEmail() { return email; }
    public InvitationRole getRole() { return role; }
    public InvitationStatus getStatus() { return status; }
}