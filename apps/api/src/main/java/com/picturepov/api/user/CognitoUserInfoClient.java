package com.picturepov.api.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
class CognitoUserInfoClient {

    private final RestClient restClient = RestClient.create();
    private final String userInfoUri;

    CognitoUserInfoClient(@Value("${app.cognito.user-info-uri}") String userInfoUri) {
        this.userInfoUri = userInfoUri;
    }

    UserInfo fetchUserInfo(String accessToken) {
        UserInfo info = restClient.get()
                .uri(userInfoUri)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .body(UserInfo.class);

        if (info == null || info.email() == null) {
            throw new IllegalStateException("Cognito userInfo returned no email");
        }

        return info;
    }

    /** Cognito returns {@code email_verified} as a string or a boolean, depending on the source of the user. */
    record UserInfo(String email, @JsonProperty("email_verified") String emailVerified) {
        boolean isEmailVerified() {
            return "true".equalsIgnoreCase(emailVerified);
        }
    }
}