package com.picturepov.api.event;

import com.picturepov.api.common.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
class EventService {

    private final EventRepository eventRepository;

    EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    PublicEventResponse getPublicEvent(String publicCode) {
        return eventRepository.findByPublicCode(publicCode)
                .filter(event -> event.getStatus() == EventStatus.ACTIVE)
                .map(PublicEventResponse::from)
                .orElseThrow(() -> new NotFoundException("EVENT_NOT_FOUND", "Event not found"));
    }
}