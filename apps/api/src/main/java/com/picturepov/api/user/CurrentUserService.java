package com.picturepov.api.user;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
class CurrentUserService {
    private final UserRepository userRepository;
    private final CognitoUserInfoClient userInfoClient;

    CurrentUserService(UserRepository userRepository, CognitoUserInfoClient userInfoClient) {
        this.userRepository = userRepository;
        this.userInfoClient = userInfoClient;
    }

    CurrentUserResponse getCurrentUser(Jwt jwt) {
        User user = findOrCreateFromToken(jwt);
        return new CurrentUserResponse(user.getId(), user.getEmail());
    }

    public User findOrCreateFromToken(Jwt jwt) {
        UUID id = UUID.fromString(Objects.requireNonNull(jwt.getSubject(), "Token has no sub claim"));

        Optional<User> existing = userRepository.findById(id);
        if (existing.isPresent()) {
            return existing.get();
        }

        CognitoUserInfoClient.UserInfo info = userInfoClient.fetchUserInfo(jwt.getTokenValue());
        if (!info.isEmailVerified()) {
            throw new IllegalStateException("Cognito email is not verified");
        }
        return userRepository.save(new User(id, info.email().trim().toLowerCase(Locale.ROOT)));
    }
}
