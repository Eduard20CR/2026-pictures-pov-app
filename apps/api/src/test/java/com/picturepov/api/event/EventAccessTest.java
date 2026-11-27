package com.picturepov.api.event;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.invitation.InvitationRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;
import java.util.UUID;

import static com.picturepov.api.invitation.InvitationRole.MODERATOR;
import static com.picturepov.api.invitation.InvitationRole.OWNER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventAccessTest {

    @Mock EventRepository eventRepository;
    @InjectMocks EventAccess eventAccess;

    UUID eventId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @ParameterizedTest
    @EnumSource(InvitationRole.class)
    void userWithAnAcceptedInvitationGetsTheirRole(InvitationRole invitedRole) {
        // Arrange
        when(eventRepository.findAcceptedInvitationRole(eventId, userId)).thenReturn(Optional.of(invitedRole));

        // Act
        InvitationRole role = eventAccess.getUserRoleInEvent(eventId, userId);

        // Assert
        assertThat(role).isEqualTo(invitedRole);
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404SoTheEventStaysHidden() {
        // Arrange
        when(eventRepository.findAcceptedInvitationRole(eventId, userId)).thenReturn(Optional.empty());

        // Act
        Throwable thrown = catchThrowable(() -> eventAccess.getUserRoleInEvent(eventId, userId));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    @Test
    void organizerMayDoAnOrganizerOnlyAction() {
        // Arrange
        when(eventRepository.findAcceptedInvitationRole(eventId, userId)).thenReturn(Optional.of(OWNER));

        // Act
        Throwable thrown = catchThrowable(() -> eventAccess.requireOrganizer(eventId, userId));

        // Assert
        assertThat(thrown).isNull();
    }

    @Test
    void moderatorIsRefusedAnOrganizerOnlyAction() {
        // Arrange
        when(eventRepository.findAcceptedInvitationRole(eventId, userId)).thenReturn(Optional.of(MODERATOR));

        // Act
        Throwable thrown = catchThrowable(() -> eventAccess.requireOrganizer(eventId, userId));

        // Assert
        assertThat(thrown).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404OnAnOrganizerOnlyAction() {
        // Arrange
        when(eventRepository.findAcceptedInvitationRole(eventId, userId)).thenReturn(Optional.empty());

        // Act
        Throwable thrown = catchThrowable(() -> eventAccess.requireOrganizer(eventId, userId));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class);
    }
}
