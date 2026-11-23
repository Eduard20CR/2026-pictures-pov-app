package com.picturepov.api.event;

import com.picturepov.api.invitation.InvitationRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByPublicCode(String publicCode);

    @Query("""
        SELECT new com.picturepov.api.event.AdminEventResponse(
            e.id, e.publicCode, e.name, e.eventDate, e.status, i.email)
        FROM Event e
        LEFT JOIN Invitation i
        ON i.eventId = e.id
        AND i.role = com.picturepov.api.invitation.InvitationRole.OWNER
        """)
    Page<AdminEventResponse> findAllWithOrganizerEmail(Pageable pageable);

    /**
     * The role the user holds on the event through an Accepted invitation (RN-04).
     * The join on Event is required: it applies Event's soft-delete filter, so a deleted event yields nothing.
     */
    @Query("""
        SELECT i.role
        FROM Invitation i
        JOIN Event e ON e.id = i.eventId
        WHERE e.id = :eventId
        AND i.userId = :userId
        AND i.status = com.picturepov.api.invitation.InvitationStatus.ACCEPTED
        """)
    Optional<InvitationRole> findAcceptedInvitationRole(UUID eventId, UUID userId);

    /** Same rule as {@link #findAcceptedInvitationRole}, for every event of the user. */
    @Query("""
        SELECT new com.picturepov.api.event.MyEventResponse(
            e.id, e.publicCode, e.name, e.eventDate, e.status, i.role)
        FROM Invitation i
        JOIN Event e ON e.id = i.eventId
        WHERE i.userId = :userId
        AND i.status = com.picturepov.api.invitation.InvitationStatus.ACCEPTED
        ORDER BY e.eventDate DESC, e.createdAt DESC
        """)
    List<MyEventResponse> findEventsWithAcceptedInvitation(UUID userId);
}
