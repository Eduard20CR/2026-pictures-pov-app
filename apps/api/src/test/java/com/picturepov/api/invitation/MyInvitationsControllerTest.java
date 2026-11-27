package com.picturepov.api.invitation;

import com.picturepov.api.configuration.WebLayerTestConfiguration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The HTTP contract only; the acceptance rules are in MyInvitationsServiceTest. */
@WebMvcTest(MyInvitationsController.class)
@Import(WebLayerTestConfiguration.class)
class MyInvitationsControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean MyInvitationsService myInvitationsService;

    UUID userId = UUID.randomUUID();
    UUID invitationId = UUID.randomUUID();

    @Test
    void listingAnswers200WithThePendingInvitations() throws Exception {
        // Arrange
        UUID eventId = UUID.randomUUID();
        when(myInvitationsService.listPending(any(Jwt.class))).thenReturn(
                List.of(new PendingInvitationResponse(invitationId, eventId, "Boda Ana y Luis", InvitationRole.MODERATOR)));

        // Act
        ResultActions result = mockMvc.perform(get("/me/invitations").with(signedIn()));

        // Assert
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(invitationId.toString()))
                .andExpect(jsonPath("$[0].eventId").value(eventId.toString()))
                .andExpect(jsonPath("$[0].eventName").value("Boda Ana y Luis"))
                .andExpect(jsonPath("$[0].role").value("MODERATOR"));
    }

    @Test
    void acceptingAnswers204() throws Exception {
        // Act
        ResultActions result = updateStatus("{\"status\": \"ACCEPTED\"}");

        // Assert
        result.andExpect(status().isNoContent());
        verify(myInvitationsService).accept(eq(invitationId), argThat(jwt -> userId.toString().equals(jwt.getSubject())));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{\"status\": \"PENDING\"}", "{}"})
    void anythingButAcceptingAnswers400(String body) throws Exception {
        // Act
        ResultActions result = updateStatus(body);

        // Assert
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        verifyNoInteractions(myInvitationsService);
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        // Act
        ResultActions list = mockMvc.perform(get("/me/invitations"));
        ResultActions accept = mockMvc.perform(patch("/me/invitations/{id}", invitationId)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"ACCEPTED\"}"));

        // Assert
        list.andExpect(status().isUnauthorized());
        accept.andExpect(status().isUnauthorized());
        verifyNoInteractions(myInvitationsService);
    }

    private ResultActions updateStatus(String body) throws Exception {
        return mockMvc.perform(patch("/me/invitations/{id}", invitationId).with(signedIn())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private RequestPostProcessor signedIn() {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }
}
