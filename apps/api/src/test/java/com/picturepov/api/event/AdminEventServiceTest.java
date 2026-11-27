package com.picturepov.api.event;

import com.picturepov.api.invitation.Invitation;
import com.picturepov.api.invitation.InvitationFixtures;
import com.picturepov.api.invitation.InvitationRepository;
import com.picturepov.api.invitation.InvitationRole;
import com.picturepov.api.invitation.InvitationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminEventServiceTest {

    @Mock EventRepository eventRepository;
    @Mock InvitationRepository invitationRepository;

    AdminEventService sut;

    @BeforeEach
    void setUp() {
        sut = new AdminEventService(
                eventRepository, new PublicCodeGenerator(), InvitationFixtures.invitationService(invitationRepository));
    }

    @Test
    void adminCreatesAnOpenEventWithAPublicCodeForTheQr() {
        // Arrange
        when(eventRepository.save(any(Event.class))).thenAnswer(call -> call.getArgument(0));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(call -> call.getArgument(0));
        CreateEventRequest request = new CreateEventRequest("  Boda Ana y Luis ", LocalDate.of(2099, 12, 20), "Ana@Example.com");

        // Act
        AdminEventResponse response = sut.createEvent(request);

        // Assert
        assertThat(response.status()).isEqualTo(EventStatus.OPEN);
        assertThat(response.name()).isEqualTo("Boda Ana y Luis");
        assertThat(response.eventDate()).isEqualTo(LocalDate.of(2099, 12, 20));
        assertThat(response.publicCode()).hasSize(22).matches("[A-Za-z0-9_-]+");
    }

    @Test
    void theOrganizerEmailGetsAPendingOwnerInvitationToTheNewEvent() {
        // Arrange
        when(eventRepository.save(any(Event.class))).thenAnswer(call -> call.getArgument(0));
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(call -> call.getArgument(0));
        ArgumentCaptor<Invitation> stored = ArgumentCaptor.forClass(Invitation.class);

        // Act
        AdminEventResponse response = sut.createEvent(
                new CreateEventRequest("Boda", LocalDate.of(2099, 12, 20), " Ana@Example.com "));

        // Assert
        verify(invitationRepository).save(stored.capture());
        assertThat(stored.getValue().getEventId()).isEqualTo(response.id());
        assertThat(stored.getValue().getEmail()).isEqualTo("ana@example.com");
        assertThat(stored.getValue().getRole()).isEqualTo(InvitationRole.OWNER);
        assertThat(stored.getValue().getStatus()).isEqualTo(InvitationStatus.PENDING);
        assertThat(response.organizerEmail()).isEqualTo("ana@example.com");
    }

    @Test
    void listsTheRequestedPageNewestEventDateFirst() {
        // Arrange
        AdminEventResponse event = new AdminEventResponse(UUID.randomUUID(), "code", "Boda", LocalDate.of(2099, 12, 20),
                EventStatus.OPEN, "ana@example.com");
        when(eventRepository.findAllWithOrganizerEmail(any(Pageable.class)))
                .thenAnswer(call -> new PageImpl<>(List.of(event), call.getArgument(0), 11));
        ArgumentCaptor<Pageable> asked = ArgumentCaptor.forClass(Pageable.class);

        // Act
        AdminEventPage page = sut.listAllEvents(2, 5);

        // Assert
        assertThat(page).isEqualTo(new AdminEventPage(List.of(event), 2, 5, 11, 3));
        verify(eventRepository).findAllWithOrganizerEmail(asked.capture());
        assertThat(asked.getValue()).isEqualTo(
                PageRequest.of(2, 5, Sort.by(Sort.Order.desc("eventDate"), Sort.Order.desc("id"))));
    }
}
