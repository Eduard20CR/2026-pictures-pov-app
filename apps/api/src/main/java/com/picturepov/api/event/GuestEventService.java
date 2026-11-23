package com.picturepov.api.event;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Events as guests reach them: by public code, only while Open. */
@Service
public class GuestEventService {

    private final EventRepository eventRepository;

    GuestEventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Transactional(readOnly = true)
    public Event getOpenEvent(String publicCode) {
        Event event = eventRepository.findByPublicCode(publicCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));

        if (event.getStatus() == EventStatus.CLOSED) {
            throw new ConflictException(ErrorCode.EVENT_CLOSED, "Event no longer accepts photos");
        }
        if (event.getStatus() != EventStatus.OPEN) {
            throw new NotFoundException(ErrorCode.EVENT_NOT_FOUND, "Event not found");
        }
        return event;
    }

    @Transactional(readOnly = true)
    GuestEventResponse getGuestEvent(String publicCode) {
        return GuestEventResponse.from(getOpenEvent(publicCode));
    }
}
