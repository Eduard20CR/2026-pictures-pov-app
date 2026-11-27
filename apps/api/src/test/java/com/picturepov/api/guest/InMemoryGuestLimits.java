package com.picturepov.api.guest;

import com.picturepov.api.event.GuestEventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** guest_limits in memory, with the SQL's rule: a slot is reserved while the count is below the limit (GuestLimitRepositoryTest). */
public class InMemoryGuestLimits extends GuestLimitRepository {

    private final Map<List<UUID>, Integer> counts = new HashMap<>();

    public InMemoryGuestLimits() {
        super(null);
    }

    /** A GuestQuotaService over an empty in-memory guest_limits. */
    public static GuestQuotaService guestQuotaService(GuestEventService guestEventService) {
        return new GuestQuotaService(new InMemoryGuestLimits(), guestEventService);
    }

    @Override
    boolean tryReserveUploadSlot(UUID eventId, UUID guestId, int limit) {
        int used = countUploadsByGuest(eventId, guestId);
        if (used >= limit) {
            return false;
        }
        counts.put(List.of(eventId, guestId), used + 1);
        return true;
    }

    @Override
    int countUploadsByGuest(UUID eventId, UUID guestId) {
        return counts.getOrDefault(List.of(eventId, guestId), 0);
    }
}
