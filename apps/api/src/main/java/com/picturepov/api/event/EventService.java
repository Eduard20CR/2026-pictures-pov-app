package com.picturepov.api.event;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class EventService {

    private final EventRepository eventRepository;

    EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public Event getActiveEvent(String publicCode) {
        return eventRepository.findByPublicCode(publicCode)
                .filter(event -> event.getStatus() == EventStatus.ACTIVE)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
    }

    @Transactional(readOnly = true)
    PublicEventResponse getPublicEvent(String publicCode) {
        return PublicEventResponse.from(getActiveEvent(publicCode));
    }
}