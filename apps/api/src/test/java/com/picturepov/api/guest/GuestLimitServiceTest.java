package com.picturepov.api.guest;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.event.Event;
import com.picturepov.api.event.EventService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestLimitServiceTest {

    @Mock GuestLimitRepository guestLimitRepository;
    @Mock EventService eventService;

    @InjectMocks GuestLimitService guestLimitService;

    UUID eventId = UUID.randomUUID();
    UUID guestId = UUID.randomUUID();

    @Test
    void throwsWhenTheGuestHasNoUploadsLeft() {
        when(guestLimitRepository.reserveUpload(eventId, guestId, 10)).thenReturn(false);

        assertThatThrownBy(() -> guestLimitService.reserveUpload(eventId, guestId, 10))
                .isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.UPLOAD_LIMIT_REACHED);
    }

    @Test
    void quotaShowsWhatIsLeft() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(event.getImagesPerUser()).thenReturn(10);
        when(eventService.getActiveEvent("abc")).thenReturn(event);
        when(guestLimitRepository.countUploads(eventId, guestId)).thenReturn(3);

        QuotaResponse quota = guestLimitService.getQuota("abc", guestId);

        assertThat(quota).isEqualTo(new QuotaResponse(10, 3, 7));
    }
}
