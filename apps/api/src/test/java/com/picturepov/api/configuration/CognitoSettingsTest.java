package com.picturepov.api.configuration;

import com.picturepov.api.ApiApplication;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// API-17: outside the local profile there is no fallback to the dev user pool.
class CognitoSettingsTest {

    @Test
    void apiFailsToStartWithoutCognitoSettingsOutsideLocalProfile() {
        assertThat(System.getenv("COGNITO_CLIENT_ID")).as("test needs COGNITO_CLIENT_ID unset").isNull();

        // Command-line arguments override the local profile that Maven activates for tests.
        assertThatThrownBy(() -> new SpringApplicationBuilder(ApiApplication.class)
                .run("--spring.profiles.active=prod", "--server.port=0"))
                .rootCause()
                .hasMessageContaining("COGNITO_");
    }
}
