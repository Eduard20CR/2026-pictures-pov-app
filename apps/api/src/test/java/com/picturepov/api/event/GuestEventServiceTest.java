package com.picturepov.api.event;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GuestEventServiceTest {

    @Mock
    EventRepository eventRepository;

    @InjectMocks
    GuestEventService guestEventService;

    @Test
    void returnsOpenEvent() {
        Event event = mock(Event.class);
        when(event.getStatus()).thenReturn(EventStatus.OPEN);
        when(event.getName()).thenReturn("Boda de prueba");
        when(eventRepository.findByPublicCode("abc")).thenReturn(Optional.of(event));

        GuestEventResponse response = guestEventService.getGuestEvent("abc");

        assertThat(response.name()).isEqualTo("Boda de prueba");
    }

    @Test
    void throwsWhenEventDoesNotExist() {
        when(eventRepository.findByPublicCode("abc")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> guestEventService.getGuestEvent("abc"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void throwsNotFoundWhenEventIsArchived() {
        Event event = mock(Event.class);
        when(event.getStatus()).thenReturn(EventStatus.ARCHIVED);
        when(eventRepository.findByPublicCode("abc")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> guestEventService.getGuestEvent("abc"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void throwsConflictWhenEventIsClosed(){
        Event event = mock(Event.class);
        when(event.getStatus()).thenReturn(EventStatus.CLOSED);
        when(eventRepository.findByPublicCode("abc")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> guestEventService.getGuestEvent("abc"))
                .isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_CLOSED);
    }
}