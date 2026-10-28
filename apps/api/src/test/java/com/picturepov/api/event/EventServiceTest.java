package com.picturepov.api.event;

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
class EventServiceTest {

    @Mock
    EventRepository eventRepository;

    @InjectMocks
    EventService eventService;

    @Test
    void returnsActiveEvent() {
        Event event = mock(Event.class);
        when(event.getStatus()).thenReturn(EventStatus.ACTIVE);
        when(event.getName()).thenReturn("Boda de prueba");
        when(eventRepository.findByPublicCode("abc")).thenReturn(Optional.of(event));

        PublicEventResponse response = eventService.getPublicEvent("abc");

        assertThat(response.name()).isEqualTo("Boda de prueba");
    }

    @Test
    void throwsWhenEventDoesNotExist() {
        when(eventRepository.findByPublicCode("abc")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.getPublicEvent("abc"))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void throwsWhenEventIsNotActive() {
        Event event = mock(Event.class);
        when(event.getStatus()).thenReturn(EventStatus.DRAFT);
        when(eventRepository.findByPublicCode("abc")).thenReturn(Optional.of(event));

        assertThatThrownBy(() -> eventService.getPublicEvent("abc"))
                .isInstanceOf(NotFoundException.class);
    }
}