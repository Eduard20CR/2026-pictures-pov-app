package com.picturepov.api.guest;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.event.Event;
import com.picturepov.api.event.EventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GuestLimitService {

    private final GuestLimitRepository guestLimitRepository;
    private final EventService eventService;

    GuestLimitService(GuestLimitRepository guestLimitRepository, EventService eventService) {
        this.guestLimitRepository = guestLimitRepository;
        this.eventService = eventService;
    }

    @Transactional
    public void reserveUpload(UUID eventId, UUID guestId, int limit) {
        boolean reserved = guestLimitRepository.reserveUpload(eventId, guestId, limit);
        if (!reserved) {
            throw new ConflictException(ErrorCode.UPLOAD_LIMIT_REACHED, "Upload limit reached for this event");
        }
    }

    @Transactional(readOnly = true)
    QuotaResponse getQuota(String publicCode, UUID guestId) {
        Event event = eventService.getActiveEvent(publicCode);
        int used = guestLimitRepository.countUploads(event.getId(), guestId);
        return QuotaResponse.of(event.getImagesPerUser(), used);
    }




}
