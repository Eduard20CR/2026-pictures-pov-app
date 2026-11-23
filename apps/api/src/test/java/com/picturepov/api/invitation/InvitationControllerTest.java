package com.picturepov.api.invitation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.picturepov.api.invitation.InvitationRole.MODERATOR;
import static com.picturepov.api.invitation.InvitationRole.OWNER;
import static com.picturepov.api.invitation.InvitationStatus.ACCEPTED;
import static com.picturepov.api.invitation.InvitationStatus.PENDING;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class InvitationControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcClient jdbc;

    @Test
    void organizerInvitesAModeratorWithoutAnAccount() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);

        // correr + revisar
        inviteModerator(event, organizer, "Beto@Example.COM")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.email").value("beto@example.com"))
                .andExpect(jsonPath("$.role").value("MODERATOR"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void emailWithAnInvitationToTheEventIsRejected() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        inviteModerator(event, organizer, "beto@example.com").andExpect(status().isCreated());

        // correr + revisar
        inviteModerator(event, organizer, "BETO@example.com")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVITATION_ALREADY_EXISTS"));
        inviteModerator(event, organizer, "Ana@Example.com")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVITATION_ALREADY_EXISTS"));
    }

    @Test
    void moderatorCannotInviteAndGets403() throws Exception {
        // guion
        UUID event = createEvent();
        UUID moderator = createUser("beto@example.com");
        invite(event, moderator, MODERATOR, ACCEPTED);

        // correr + revisar
        inviteModerator(event, moderator, "carla@example.com")
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404() throws Exception {
        // guion
        UUID event = createEvent();
        invite(event, createUser("ana@example.com"), OWNER, ACCEPTED);
        UUID pendingOwner = createUser("beto@example.com");
        UUID otherEvent = createEvent();
        invite(otherEvent, pendingOwner, OWNER, PENDING);

        // correr + revisar
        inviteModerator(event, pendingOwner, "carla@example.com")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
        inviteModerator(otherEvent, pendingOwner, "carla@example.com")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
    }

    private ResultActions inviteModerator(UUID eventId, UUID userId, String email) throws Exception {
        return mockMvc.perform(post("/me/events/{id}/invitations", eventId)
                .with(jwt().jwt(token -> token.subject(userId.toString())))
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\": \"" + email + "\"}"));
    }

    private UUID createEvent() {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO events (id, name, color, public_code, event_date, images_per_user, status)
                VALUES (:id, 'Boda Ana y Luis', '#000000', :code, DATE '2099-12-12', 10, 'OPEN')
                """)
                .param("id", id).param("code", id.toString().substring(0, 22))
                .update();
        return id;
    }

    private UUID createUser(String email) {
        UUID id = UUID.randomUUID();
        jdbc.sql("INSERT INTO users (id, email) VALUES (:id, :email)")
                .param("id", id).param("email", email).update();
        return id;
    }

    /** An invitation for the user's email, linked to the user. */
    private void invite(UUID eventId, UUID userId, InvitationRole role, InvitationStatus status) {
        jdbc.sql("""
                INSERT INTO invitations (id, event_id, email, user_id, role, status)
                SELECT :id, :eventId, email, id, :role, :status FROM users WHERE id = :userId
                """)
                .param("id", UUID.randomUUID()).param("eventId", eventId).param("userId", userId)
                .param("role", role.name()).param("status", status.name())
                .update();
    }
}
