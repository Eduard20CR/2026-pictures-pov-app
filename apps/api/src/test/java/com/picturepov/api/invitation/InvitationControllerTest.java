package com.picturepov.api.invitation;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.configuration.WebLayerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.UUID;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the invitation rules are in InvitationServiceTest. */
@WebMvcTest(InvitationController.class)
@Import(WebLayerTestConfiguration.class)
class InvitationControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean InvitationService invitationService;

    UUID eventId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @Test
    void invitingAnswers201WithThePendingInvitation() throws Exception {
        // Arrange
        UUID invitationId = UUID.randomUUID();
        when(invitationService.inviteModerator(eventId, userId, "Beto@Example.com")).thenReturn(
                new InvitationResponse(invitationId, "beto@example.com", InvitationRole.MODERATOR, InvitationStatus.PENDING));

        // Act
        ResultActions result = invite("Beto@Example.com");

        // Assert
        result.andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(invitationId.toString()))
                .andExpect(jsonPath("$.email").value("beto@example.com"))
                .andExpect(jsonPath("$.role").value("MODERATOR"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void invalidEmailAnswers400AndInvitesNobody() throws Exception {
        // Act
        ResultActions result = invite("not-an-email");

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"))
                .andExpect(jsonPath("$.errors[0].field").value("email"));
        verifyNoInteractions(invitationService);
    }

    @Test
    void duplicateInvitationAnswers409WithItsCode() throws Exception {
        // Arrange
        when(invitationService.inviteModerator(eventId, userId, "beto@example.com"))
                .thenThrow(new ConflictException(ErrorCode.INVITATION_ALREADY_EXISTS, "Already invited"));

        // Act
        ResultActions result = invite("beto@example.com");

        // Assert
        result.andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVITATION_ALREADY_EXISTS"));
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        // Act
        ResultActions result = mockMvc.perform(post("/me/events/{id}/invitations", eventId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"beto@example.com\"}"));

        // Assert
        result.andExpect(status().isUnauthorized());
        verifyNoInteractions(invitationService);
    }

    private ResultActions invite(String email) throws Exception {
        return mockMvc.perform(post("/me/events/{id}/invitations", eventId)
                .with(jwt().jwt(token -> token.subject(userId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\"}"));
    }
}
