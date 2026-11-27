package com.picturepov.api.event;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static com.picturepov.api.event.EventFixtures.PUBLIC_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestEventServiceTest {

    @Mock EventRepository eventRepository;
    @InjectMocks GuestEventService guestEventService;

    @Test
    void guestSeesTheOpenEvent() {
        // Arrange
        when(eventRepository.findByPublicCode(PUBLIC_CODE)).thenReturn(Optional.of(EventFixtures.openEvent(10)));

        // Act
        GuestEventResponse response = guestEventService.getGuestEvent(PUBLIC_CODE);

        // Assert
        assertThat(response).isEqualTo(new GuestEventResponse(
                PUBLIC_CODE, "Boda Ana y Luis", "#000000", LocalDate.of(2099, 12, 12), 10));
    }

    @Test
    void unknownCodeGets404() {
        // Arrange
        when(eventRepository.findByPublicCode(PUBLIC_CODE)).thenReturn(Optional.empty());

        // Act
        Throwable thrown = catchThrowable(() -> guestEventService.getGuestEvent(PUBLIC_CODE));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    void archivedEventGets404AsIfItDidNotExist() {
        // Arrange
        when(eventRepository.findByPublicCode(PUBLIC_CODE))
                .thenReturn(Optional.of(EventFixtures.event(EventStatus.ARCHIVED, 10)));

        // Act
        Throwable thrown = catchThrowable(() -> guestEventService.getGuestEvent(PUBLIC_CODE));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    void closedEventGets409BecauseItNoLongerAcceptsPhotos() {
        // Arrange
        when(eventRepository.findByPublicCode(PUBLIC_CODE))
                .thenReturn(Optional.of(EventFixtures.event(EventStatus.CLOSED, 10)));

        // Act
        Throwable thrown = catchThrowable(() -> guestEventService.getGuestEvent(PUBLIC_CODE));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_CLOSED);
    }
}
