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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock CognitoUserInfoClient userInfoClient;

    @InjectMocks UserService userService;

    @Test
    void existingUserDoesNotCallCognito() {
        UUID id = UUID.randomUUID();
        User existing = new User(id, "owner@example.com");
        when(userRepository.findById(id)).thenReturn(Optional.of(existing));

        User user = userService.findOrCreate(jwtFor(id));

        assertThat(user).isSameAs(existing);
        verifyNoInteractions(userInfoClient);
    }

    private static Jwt jwtFor(UUID sub) {
        return Jwt.withTokenValue("token").header("alg", "none").subject(sub.toString()).build();
    }
}
