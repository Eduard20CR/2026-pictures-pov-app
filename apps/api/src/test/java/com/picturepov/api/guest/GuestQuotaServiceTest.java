package com.picturepov.api.guest;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.event.Event;
import com.picturepov.api.event.EventFixtures;
import com.picturepov.api.event.EventStatus;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.picturepov.api.event.EventFixtures.PUBLIC_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class GuestQuotaServiceTest {

    InMemoryGuestLimits guestLimits = new InMemoryGuestLimits();
    UUID guestId = UUID.randomUUID();

    @Test
    void guestReservesUploadsUntilTheLimit() {
        // Arrange
        Event event = EventFixtures.openEvent(2);
        GuestQuotaService service = serviceFor(event);
        service.reserveUploadSlot(event.getId(), guestId, 2);
        service.reserveUploadSlot(event.getId(), guestId, 2);

        // Act
        Throwable thrown = catchThrowable(() -> service.reserveUploadSlot(event.getId(), guestId, 2));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.UPLOAD_LIMIT_REACHED);
    }

    @Test
    void eachGuestHasTheirOwnLimit() {
        // Arrange
        Event event = EventFixtures.openEvent(1);
        GuestQuotaService service = serviceFor(event);
        service.reserveUploadSlot(event.getId(), guestId, 1);

        // Act
        Throwable thrown = catchThrowable(() -> service.reserveUploadSlot(event.getId(), UUID.randomUUID(), 1));

        // Assert
        assertThat(thrown).isNull();
    }

    @Test
    void quotaShowsTheUploadsLeft() {
        // Arrange
        Event event = EventFixtures.openEvent(10);
        GuestQuotaService service = serviceFor(event);
        for (int i = 0; i < 3; i++) {
            service.reserveUploadSlot(event.getId(), guestId, 10);
        }

        // Act
        UploadQuotaResponse quota = service.getUploadQuota(PUBLIC_CODE, guestId);

        // Assert
        assertThat(quota).isEqualTo(new UploadQuotaResponse(10, 3, 7));
    }

    @Test
    void remainingNeverGoesBelowZeroWhenTheLimitIsLowered() {
        // Arrange: the guest used 3 of 3, then the organizer lowered the limit to 2
        Event event = EventFixtures.openEvent(2);
        GuestQuotaService service = serviceFor(event);
        for (int i = 0; i < 3; i++) {
            service.reserveUploadSlot(event.getId(), guestId, 3);
        }

        // Act
        UploadQuotaResponse quota = service.getUploadQuota(PUBLIC_CODE, guestId);

        // Assert
        assertThat(quota).isEqualTo(new UploadQuotaResponse(2, 3, 0));
    }

    @Test
    void closedEventHasNoQuota() {
        // Arrange
        GuestQuotaService service = serviceFor(EventFixtures.event(EventStatus.CLOSED, 10));

        // Act
        Throwable thrown = catchThrowable(() -> service.getUploadQuota(PUBLIC_CODE, guestId));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_CLOSED);
    }

    private GuestQuotaService serviceFor(Event event) {
        return new GuestQuotaService(guestLimits, EventFixtures.guestEventService(event));
    }
}
