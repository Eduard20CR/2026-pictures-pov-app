package com.picturepov.api.guest;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.event.Event;
import com.picturepov.api.event.GuestEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class GuestQuotaService {

    private final GuestLimitRepository guestLimitRepository;
    private final GuestEventService guestEventService;

    GuestQuotaService(GuestLimitRepository guestLimitRepository, GuestEventService guestEventService) {
        this.guestLimitRepository = guestLimitRepository;
        this.guestEventService = guestEventService;
    }

    @Transactional
    public void reserveUploadSlot(UUID eventId, UUID guestId, int limit) {
        boolean reserved = guestLimitRepository.tryReserveUploadSlot(eventId, guestId, limit);
        if (!reserved) {
            throw new ConflictException(ErrorCode.UPLOAD_LIMIT_REACHED, "Upload limit reached for this event");
        }
    }

    @Transactional(readOnly = true)
    UploadQuotaResponse getUploadQuota(String publicCode, UUID guestId) {
        Event event = guestEventService.getOpenEvent(publicCode);
        int used = guestLimitRepository.countUploadsByGuest(event.getId(), guestId);
        return UploadQuotaResponse.of(event.getImagesPerUser(), used);
    }




}
