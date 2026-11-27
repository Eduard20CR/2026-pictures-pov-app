package com.picturepov.api.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock UserRepository userRepository;
    @Mock CognitoUserInfoClient cognito;
    @InjectMocks CurrentUserService currentUserService;

    UUID sub = UUID.randomUUID();
    Jwt jwt = Jwt.withTokenValue("token").header("alg", "none").subject(sub.toString()).build();

    @Test
    void existingUserIsReturnedWithoutCallingCognito() {
        // Arrange
        when(userRepository.findById(sub)).thenReturn(Optional.of(new User(sub, "ana@example.com")));

        // Act
        CurrentUserResponse user = currentUserService.getCurrentUser(jwt);

        // Assert
        assertThat(user).isEqualTo(new CurrentUserResponse(sub, "ana@example.com"));
        verifyNoInteractions(cognito);
    }

    @Test
    void firstSignInStoresTheUserUnderTheCognitoSubWithTheEmailLowercased() {
        // Arrange
        when(userRepository.findById(sub)).thenReturn(Optional.empty());
        when(cognito.fetchUserInfo("token")).thenReturn(new CognitoUserInfoClient.UserInfo(" Ana@X.com ", "true"));
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        CurrentUserResponse user = currentUserService.getCurrentUser(jwt);

        // Assert
        assertThat(user).isEqualTo(new CurrentUserResponse(sub, "ana@x.com"));
    }

    @Test
    void unverifiedEmailIsNotStored() {
        // Arrange
        when(userRepository.findById(sub)).thenReturn(Optional.empty());
        when(cognito.fetchUserInfo("token")).thenReturn(new CognitoUserInfoClient.UserInfo("ana@x.com", "false"));

        // Act
        Throwable thrown = catchThrowable(() -> currentUserService.findOrCreateFromToken(jwt));

        // Assert
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifiedEmailIsReadLiveFromCognitoInLowercase() {
        // Arrange
        when(cognito.fetchUserInfo("token")).thenReturn(new CognitoUserInfoClient.UserInfo("Ana@X.com", "TRUE"));

        // Act
        Optional<String> email = currentUserService.findVerifiedEmail(jwt);

        // Assert
        assertThat(email).contains("ana@x.com");
    }

    @Test
    void unverifiedEmailIsNotReturned() {
        // Arrange
        when(cognito.fetchUserInfo("token")).thenReturn(new CognitoUserInfoClient.UserInfo("ana@x.com", "false"));

        // Act
        Optional<String> email = currentUserService.findVerifiedEmail(jwt);

        // Assert
        assertThat(email).isEmpty();
    }
}
