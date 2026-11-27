package com.picturepov.api.invitation;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.event.EventFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvitationServiceTest {

    @Mock InvitationRepository invitationRepository;

    UUID eventId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @Test
    void organizerInvitesAModeratorWhoNeedsNoAccountYet() {
        // Arrange
        InvitationService sut = serviceFor(InvitationRole.OWNER);
        when(invitationRepository.existsByEventIdAndEmail(eventId, "beto@example.com")).thenReturn(false);
        when(invitationRepository.saveAndFlush(any(Invitation.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        InvitationResponse response = sut.inviteModerator(eventId, userId, " Beto@Example.COM ");

        // Assert
        assertThat(response.id()).isNotNull();
        assertThat(response.email()).isEqualTo("beto@example.com");
        assertThat(response.role()).isEqualTo(InvitationRole.MODERATOR);
        assertThat(response.status()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    void emailThatAlreadyHasAnInvitationToTheEventGets409() {
        // Arrange
        InvitationService sut = serviceFor(InvitationRole.OWNER);
        when(invitationRepository.existsByEventIdAndEmail(eventId, "beto@example.com")).thenReturn(true);

        // Act
        Throwable thrown = catchThrowable(() -> sut.inviteModerator(eventId, userId, "BETO@example.com"));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.INVITATION_ALREADY_EXISTS);
        verify(invitationRepository, never()).saveAndFlush(any(Invitation.class));
    }

    @Test
    void concurrentDuplicateInvitationGets409NotA500() {
        // Arrange: another request stored the same email between the check and the insert
        InvitationService sut = serviceFor(InvitationRole.OWNER);
        when(invitationRepository.existsByEventIdAndEmail(eventId, "beto@example.com")).thenReturn(false);
        when(invitationRepository.saveAndFlush(any(Invitation.class)))
                .thenThrow(new DataIntegrityViolationException("uq_invitations_event_email"));

        // Act
        Throwable thrown = catchThrowable(() -> sut.inviteModerator(eventId, userId, "beto@example.com"));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.INVITATION_ALREADY_EXISTS);
    }

    @Test
    void moderatorCannotInvite() {
        // Arrange
        InvitationService sut = serviceFor(InvitationRole.MODERATOR);

        // Act
        Throwable thrown = catchThrowable(() -> sut.inviteModerator(eventId, userId, "carla@example.com"));

        // Assert
        assertThat(thrown).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(invitationRepository);
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404() {
        // Arrange
        InvitationService sut = serviceFor(InvitationRole.OWNER);

        // Act
        Throwable thrown = catchThrowable(() -> sut.inviteModerator(eventId, UUID.randomUUID(), "carla@example.com"));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
        verifyNoInteractions(invitationRepository);
    }

    @Test
    void ownerInvitationOfANewEventIsPendingWithTheLowercasedEmail() {
        // Arrange
        InvitationService sut = serviceFor(InvitationRole.OWNER);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        Invitation invitation = sut.createOwnerInvitation(eventId, " Ana@Example.com ");

        // Assert
        assertThat(invitation.getEventId()).isEqualTo(eventId);
        assertThat(invitation.getEmail()).isEqualTo("ana@example.com");
        assertThat(invitation.getRole()).isEqualTo(InvitationRole.OWNER);
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    /** userId holds the role on eventId; anyone else has no access. */
    private InvitationService serviceFor(InvitationRole role) {
        return new InvitationService(invitationRepository, EventFixtures.eventAccess(eventId, userId, role));
    }
}
