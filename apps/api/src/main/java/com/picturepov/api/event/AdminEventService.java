package com.picturepov.api.event;

import com.picturepov.api.invitation.Invitation;
import com.picturepov.api.invitation.InvitationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Events as the administrator manages them: create and list all. */
@Service
class AdminEventService {

    private static final Sort NEWEST_FIRST =
            Sort.by(Sort.Order.desc("eventDate"), Sort.Order.desc("id"));

    private final EventRepository eventRepository;
    private final PublicCodeGenerator publicCodeGenerator;
    private final InvitationService invitationService;

    AdminEventService(EventRepository eventRepository,
                      PublicCodeGenerator publicCodeGenerator,
                      InvitationService invitationService) {
        this.eventRepository = eventRepository;
        this.publicCodeGenerator = publicCodeGenerator;
        this.invitationService = invitationService;
    }

    @Transactional
    AdminEventResponse createEvent(CreateEventRequest request) {
        Event event = eventRepository.save(Event.createOpen(request.name(), request.eventDate(), publicCodeGenerator.generate()));
        Invitation ownerInvitation = invitationService.createOwnerInvitation(event.getId(), request.organizerEmail());
        return AdminEventResponse.from(event, ownerInvitation);
    }

    @Transactional(readOnly = true)
    AdminEventPage listAllEvents(int page, int size) {
        return AdminEventPage.from(eventRepository.findAllWithOrganizerEmail(PageRequest.of(page, size, NEWEST_FIRST)));
    }
}
