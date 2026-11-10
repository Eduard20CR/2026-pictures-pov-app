package com.picturepov.api.event;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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
    Page<AdminEventResponse> findAdminEvents(Pageable pageable);
}