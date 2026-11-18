package com.picturepov.api.user;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
class UserService {
    private final UserRepository userRepository;
    private final CognitoUserInfoClient userInfoClient;

    UserService(UserRepository userRepository, CognitoUserInfoClient userInfoClient) {
        this.userRepository = userRepository;
        this.userInfoClient = userInfoClient;
    }

    public User findOrCreate(Jwt jwt) {
        UUID id = UUID.fromString(Objects.requireNonNull(jwt.getSubject(), "Token has no sub claim"));

        Optional<User> existing = userRepository.findById(id);
        if (existing.isPresent()) {
            return existing.get();
        }

        CognitoUserInfoClient.UserInfo info = userInfoClient.fetch(jwt.getTokenValue());
        if (!info.isEmailVerified()) {
            throw new IllegalStateException("Cognito email is not verified");
        }
        return userRepository.save(new User(id, info.email().trim().toLowerCase(Locale.ROOT)));
    }
}
