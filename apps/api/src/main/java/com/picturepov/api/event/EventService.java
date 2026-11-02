package com.picturepov.api.event;

import com.picturepov.api.common.error.ConflictException;
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
        Event event = eventRepository.findByPublicCode(publicCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));

        if (event.getStatus() == EventStatus.CLOSED) {
            throw new ConflictException(ErrorCode.EVENT_CLOSED, "Event no longer accepts photos");
        }
        if (event.getStatus() != EventStatus.ACTIVE) {
            throw new NotFoundException(ErrorCode.EVENT_NOT_FOUND, "Event not found");
        }
        return event;
    }

    @Transactional(readOnly = true)
    PublicEventResponse getPublicEvent(String publicCode) {
        return PublicEventResponse.from(getActiveEvent(publicCode));
    }
}