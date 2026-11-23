package com.picturepov.api.event;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.invitation.InvitationRole;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** Events of the signed-in organizer or moderator. */
@Service
class MyEventsService {

    private final EventRepository eventRepository;
    private final EventAccess eventAccess;

    MyEventsService(EventRepository eventRepository, EventAccess eventAccess) {
        this.eventRepository = eventRepository;
        this.eventAccess = eventAccess;
    }

    /** Filters by the user, so it needs no EventAccess check (RN-04). */
    @Transactional(readOnly = true)
    List<MyEventResponse> listMyEvents(UUID userId) {
        return eventRepository.findEventsWithAcceptedInvitation(userId);
    }

    @Transactional(readOnly = true)
    MyEventResponse getMyEvent(UUID eventId, UUID userId) {
        InvitationRole role = eventAccess.getUserRoleInEvent(eventId, userId);
        // Still 404 if the event is deleted between the two reads.
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
        return new MyEventResponse(event.getId(), event.getPublicCode(), event.getName(),
                event.getEventDate(), event.getStatus(), role);
    }
}
