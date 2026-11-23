package com.picturepov.api.invitation;

import com.picturepov.api.user.CognitoUserInfoClient;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MyInvitationsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcClient jdbc;
    @MockitoBean CognitoUserInfoClient userInfoClient;

    @Test
    void verifiedUserSeesTheirPendingInvitations() throws Exception {
        // guion
        UUID event = createEvent("Boda Ana y Luis");
        UUID invitation = createPendingInvitation(event, "beto@example.com", InvitationRole.MODERATOR);
        createPendingInvitation(createEvent("Otra boda"), "carla@example.com", InvitationRole.MODERATOR);
        UUID beto = createUser("beto@example.com");
        signedInAs("Beto@Example.com", true);

        // correr + revisar
        listMyInvitations(beto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(invitation.toString()))
                .andExpect(jsonPath("$[0].eventName").value("Boda Ana y Luis"))
                .andExpect(jsonPath("$[0].role").value("MODERATOR"));
    }

    @Test
    void unverifiedEmailSeesNoPendingInvitations() throws Exception {
        // guion
        createPendingInvitation(createEvent("Boda Ana y Luis"), "beto@example.com", InvitationRole.MODERATOR);
        UUID beto = createUser("beto@example.com");
        signedInAs("beto@example.com", false);

        // correr + revisar
        listMyInvitations(beto)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void acceptingAddsTheEventToMyEventsWithTheInvitedRole() throws Exception {
        // guion
        UUID event = createEvent("Boda Ana y Luis");
        UUID invitation = createPendingInvitation(event, "beto@example.com", InvitationRole.MODERATOR);
        UUID beto = createUser("beto@example.com");
        signedInAs("BETO@example.com", true);

        // correr
        accept(invitation, beto).andExpect(status().isNoContent());

        // revisar
        listMyInvitations(beto).andExpect(jsonPath("$", hasSize(0)));
        mockMvc.perform(get("/me/events").with(as(beto)))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(event.toString()))
                .andExpect(jsonPath("$[0].role").value("MODERATOR"));
    }

    @Test
    void unverifiedEmailCannotAcceptAndTheInvitationStaysPending() throws Exception {
        // guion
        UUID invitation = createPendingInvitation(createEvent("Boda Ana y Luis"), "beto@example.com", InvitationRole.MODERATOR);
        UUID beto = createUser("beto@example.com");
        signedInAs("beto@example.com", false);

        // correr + revisar
        accept(invitation, beto)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        signedInAs("beto@example.com", true);
        listMyInvitations(beto).andExpect(jsonPath("$[0].id").value(invitation.toString()));
    }

    @Test
    void invitationForAnotherEmailCannotBeAccepted() throws Exception {
        // guion
        UUID invitation = createPendingInvitation(createEvent("Boda Ana y Luis"), "carla@example.com", InvitationRole.MODERATOR);
        UUID beto = createUser("beto@example.com");
        signedInAs("beto@example.com", true);

        // correr + revisar
        accept(invitation, beto)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mockMvc.perform(get("/me/events").with(as(beto))).andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void removedOrAcceptedInvitationCannotBeAccepted() throws Exception {
        // guion
        UUID invitation = createPendingInvitation(createEvent("Boda Ana y Luis"), "beto@example.com", InvitationRole.MODERATOR);
        UUID beto = createUser("beto@example.com");
        signedInAs("beto@example.com", true);
        accept(invitation, beto).andExpect(status().isNoContent());

        // correr + revisar
        accept(invitation, beto)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVITATION_NOT_FOUND"));
        accept(UUID.randomUUID(), beto)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVITATION_NOT_FOUND"));
    }

    @Test
    void invitationToADeletedEventCannotBeAccepted() throws Exception {
        // guion
        UUID event = createEvent("Boda Ana y Luis");
        UUID invitation = createPendingInvitation(event, "beto@example.com", InvitationRole.MODERATOR);
        jdbc.sql("UPDATE events SET deleted_at = now() WHERE id = :id").param("id", event).update();
        UUID beto = createUser("beto@example.com");
        signedInAs("beto@example.com", true);

        // correr + revisar
        accept(invitation, beto)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("INVITATION_NOT_FOUND"));
    }

    @Test
    void onlyAcceptingIsAllowed() throws Exception {
        // guion
        UUID invitation = createPendingInvitation(createEvent("Boda Ana y Luis"), "beto@example.com", InvitationRole.MODERATOR);
        UUID beto = createUser("beto@example.com");
        signedInAs("beto@example.com", true);

        // correr + revisar
        updateStatus(invitation, beto, "PENDING")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_FAILED"));
        listMyInvitations(beto).andExpect(jsonPath("$[0].id").value(invitation.toString()));
    }

    private ResultActions accept(UUID invitationId, UUID userId) throws Exception {
        return updateStatus(invitationId, userId, "ACCEPTED");
    }

    private ResultActions updateStatus(UUID invitationId, UUID userId, String status) throws Exception {
        return mockMvc.perform(patch("/me/invitations/{id}", invitationId).with(as(userId))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\": \"" + status + "\"}"));
    }

    private static RequestPostProcessor as(UUID userId) {
        return jwt().jwt(token -> token.subject(userId.toString()));
    }

    private void signedInAs(String email, boolean emailVerified) {
        when(userInfoClient.fetchUserInfo(anyString()))
                .thenReturn(new CognitoUserInfoClient.UserInfo(email, String.valueOf(emailVerified)));
    }

    private ResultActions listMyInvitations(UUID userId) throws Exception {
        return mockMvc.perform(get("/me/invitations").with(as(userId)));
    }

    private UUID createEvent(String name) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO events (id, name, color, public_code, event_date, images_per_user, status)
                VALUES (:id, :name, '#000000', :code, DATE '2099-12-12', 10, 'OPEN')
                """)
                .param("id", id).param("name", name).param("code", id.toString().substring(0, 22))
                .update();
        return id;
    }

    private UUID createUser(String email) {
        UUID id = UUID.randomUUID();
        jdbc.sql("INSERT INTO users (id, email) VALUES (:id, :email)")
                .param("id", id).param("email", email).update();
        return id;
    }

    private UUID createPendingInvitation(UUID eventId, String email, InvitationRole role) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO invitations (id, event_id, email, role, status)
                VALUES (:id, :eventId, :email, :role, 'PENDING')
                """)
                .param("id", id).param("eventId", eventId).param("email", email).param("role", role.name())
                .update();
        return id;
    }
}
