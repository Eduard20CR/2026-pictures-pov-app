package com.picturepov.api.invitation;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.user.CognitoUserInfoClient;
import com.picturepov.api.user.User;
import com.picturepov.api.user.UserFixtures;
import com.picturepov.api.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MyInvitationsServiceTest {

    @Mock InvitationRepository invitationRepository;
    @Mock UserRepository userRepository;
    @Mock CognitoUserInfoClient cognito;

    MyInvitationsService service;

    UUID userId = UUID.randomUUID();
    Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(userId.toString()).build();

    @BeforeEach
    void setUp() {
        service = new MyInvitationsService(invitationRepository, UserFixtures.currentUserService(userRepository, cognito));
    }

    @Test
    void verifiedUserSeesThePendingInvitationsForTheirEmail() {
        // Arrange
        signedInAs("Beto@Example.com", true);
        List<PendingInvitationResponse> pending = List.of(
                new PendingInvitationResponse(UUID.randomUUID(), UUID.randomUUID(), "Boda Ana y Luis", InvitationRole.MODERATOR));
        when(invitationRepository.findPendingByEmail("beto@example.com")).thenReturn(pending);

        // Act
        List<PendingInvitationResponse> invitations = service.listPending(jwt);

        // Assert
        assertThat(invitations).isEqualTo(pending);
    }

    @Test
    void unverifiedEmailSeesNoInvitations() {
        // Arrange
        signedInAs("beto@example.com", false);

        // Act
        List<PendingInvitationResponse> invitations = service.listPending(jwt);

        // Assert
        assertThat(invitations).isEmpty();
        verifyNoInteractions(invitationRepository);
    }

    @Test
    void acceptingLinksTheInvitationToTheSignedInUser() {
        // Arrange
        Invitation invitation = Invitation.createModerator(UUID.randomUUID(), "beto@example.com");
        signedInAs("BETO@example.com", true);
        when(invitationRepository.findPendingById(invitation.getId())).thenReturn(Optional.of(invitation));
        when(userRepository.findById(userId)).thenReturn(Optional.of(new User(userId, "beto@example.com")));

        // Act
        service.accept(invitation.getId(), jwt);

        // Assert
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        assertThat(invitation).extracting("userId").isEqualTo(userId);
    }

    @Test
    void unverifiedEmailCannotAccept() {
        // Arrange
        signedInAs("beto@example.com", false);

        // Act
        Throwable thrown = catchThrowable(() -> service.accept(UUID.randomUUID(), jwt));

        // Assert
        assertThat(thrown).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(invitationRepository);
    }

    @Test
    void invitationForAnotherEmailCannotBeAcceptedAndStaysPending() {
        // Arrange
        Invitation invitation = Invitation.createModerator(UUID.randomUUID(), "carla@example.com");
        signedInAs("beto@example.com", true);
        when(invitationRepository.findPendingById(invitation.getId())).thenReturn(Optional.of(invitation));

        // Act
        Throwable thrown = catchThrowable(() -> service.accept(invitation.getId(), jwt));

        // Assert
        assertThat(thrown).isInstanceOf(AccessDeniedException.class);
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    void invitationThatIsNoLongerPendingGets404() {
        // Arrange: removed, already accepted, or its event deleted
        UUID invitationId = UUID.randomUUID();
        signedInAs("beto@example.com", true);
        when(invitationRepository.findPendingById(invitationId)).thenReturn(Optional.empty());

        // Act
        Throwable thrown = catchThrowable(() -> service.accept(invitationId, jwt));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.INVITATION_NOT_FOUND);
    }

    private void signedInAs(String email, boolean emailVerified) {
        when(cognito.fetchUserInfo("token")).thenReturn(new CognitoUserInfoClient.UserInfo(email, String.valueOf(emailVerified)));
    }
}
