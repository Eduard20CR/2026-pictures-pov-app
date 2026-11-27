package com.picturepov.api.event;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.invitation.InvitationRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static com.picturepov.api.event.EventFixtures.PUBLIC_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyEventsServiceTest {

    @Mock EventRepository eventRepository;

    MyEventsService sut;

    UUID userId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sut = new MyEventsService(eventRepository, new EventAccess(eventRepository));
    }

    @ParameterizedTest
    @EnumSource(InvitationRole.class)
    void invitedUserOpensTheEventWithTheirRole(InvitationRole role) {
        // Arrange
        Event event = EventFixtures.openEvent(10);
        when(eventRepository.findAcceptedInvitationRole(event.getId(), userId)).thenReturn(Optional.of(role));
        when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));

        // Act
        MyEventResponse response = sut.getMyEvent(event.getId(), userId);

        // Assert
        assertThat(response).isEqualTo(new MyEventResponse(
                event.getId(), PUBLIC_CODE, "Boda Ana y Luis", LocalDate.of(2099, 12, 12), EventStatus.OPEN, role));
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findAcceptedInvitationRole(eventId, userId)).thenReturn(Optional.empty());

        // Act
        Throwable thrown = catchThrowable(() -> sut.getMyEvent(eventId, userId));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    void eventDeletedRightAfterTheAccessCheckStillGets404() {
        // Arrange
        UUID eventId = UUID.randomUUID();
        when(eventRepository.findAcceptedInvitationRole(eventId, userId)).thenReturn(Optional.of(InvitationRole.OWNER));
        when(eventRepository.findById(eventId)).thenReturn(Optional.empty());

        // Act
        Throwable thrown = catchThrowable(() -> sut.getMyEvent(eventId, userId));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }
}
