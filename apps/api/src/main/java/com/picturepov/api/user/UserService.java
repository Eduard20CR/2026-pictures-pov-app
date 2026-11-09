package com.picturepov.api.user;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

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

        String email = userInfoClient.fetchEmail(jwt.getTokenValue());
        return userRepository.save(new User(id, email));
    }
}
