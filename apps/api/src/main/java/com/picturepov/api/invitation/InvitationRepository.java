package com.picturepov.api.invitation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitationRepository extends JpaRepository<Invitation, UUID> {

    boolean existsByEventIdAndEmail(UUID eventId, String email);

    /** The joins on Event apply its soft-delete filter: invitations to a deleted event are left out. */
    @Query("""
        SELECT i
        FROM Invitation i
        JOIN Event e ON e.id = i.eventId
        WHERE i.id = :id
        AND i.status = com.picturepov.api.invitation.InvitationStatus.PENDING
        """)
    Optional<Invitation> findPendingById(UUID id);

    @Query("""
        SELECT new com.picturepov.api.invitation.PendingInvitationResponse(i.id, e.id, e.name, i.role)
        FROM Invitation i
        JOIN Event e ON e.id = i.eventId
        WHERE i.email = :email
        AND i.status = com.picturepov.api.invitation.InvitationStatus.PENDING
        ORDER BY i.createdAt DESC
        """)
    List<PendingInvitationResponse> findPendingByEmail(String email);
}
