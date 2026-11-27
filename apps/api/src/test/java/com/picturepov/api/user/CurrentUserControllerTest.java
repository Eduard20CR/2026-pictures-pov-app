package com.picturepov.api.user;

import com.picturepov.api.configuration.WebLayerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the sign-in rules are in CurrentUserServiceTest. */
@WebMvcTest(CurrentUserController.class)
@Import(WebLayerTestConfiguration.class)
class CurrentUserControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean CurrentUserService currentUserService;

    @Test
    void signedInUserGets200WithTheirAccount() throws Exception {
        // Arrange
        UUID sub = UUID.randomUUID();
        when(currentUserService.getCurrentUser(any(Jwt.class))).thenReturn(new CurrentUserResponse(sub, "ana@example.com"));

        // Act
        ResultActions result = mockMvc.perform(get("/me").with(jwt().jwt(token -> token.subject(sub.toString()))));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sub.toString()))
                .andExpect(jsonPath("$.email").value("ana@example.com"));
    }

    @Test
    void unexpectedFailureAnswers500WithoutInternalDetails() throws Exception {
        // Arrange
        when(currentUserService.getCurrentUser(any(Jwt.class))).thenThrow(new IllegalStateException("Cognito userInfo returned no email"));

        // Act
        ResultActions result = mockMvc.perform(get("/me").with(jwt()));

        // Assert
        result.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.detail").value("Unexpected error."));
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(get("/me"));

        // Assert
        result.andExpect(status().isUnauthorized());
        verifyNoInteractions(currentUserService);
    }
}
