package com.picturepov.api.event;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.invitation.InvitationRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/** Who reaches which event (RN-04, RN-05, RNF-SEG-02). Every endpoint that takes an event id from a signed-in user must check here first. */
@Service
public class EventAccess {

    private final EventRepository eventRepository;

    EventAccess(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /** The user's role on the event; 404 if they hold no Accepted invitation, so the event's existence is not revealed. */
    @Transactional(readOnly = true)
    public InvitationRole roleIn(UUID eventId, UUID userId) {
        return eventRepository.findAcceptedRole(eventId, userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
    }

    /** As {@link #roleIn}, plus 403 for a moderator: the action is the organizer's only (RN-05). */
    @Transactional(readOnly = true)
    public void requireOrganizer(UUID eventId, UUID userId) {
        if (roleIn(eventId, userId) != InvitationRole.OWNER) {
            throw new AccessDeniedException("Organizer only");
        }
    }
}
