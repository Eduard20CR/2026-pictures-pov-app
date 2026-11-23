package com.picturepov.api.event;

import com.picturepov.api.invitation.InvitationRole;
import com.picturepov.api.invitation.InvitationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.picturepov.api.invitation.InvitationRole.MODERATOR;
import static com.picturepov.api.invitation.InvitationRole.OWNER;
import static com.picturepov.api.invitation.InvitationStatus.ACCEPTED;
import static com.picturepov.api.invitation.InvitationStatus.PENDING;
import static org.hamcrest.Matchers.contains;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MyEventsControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcClient jdbc;

    @Test
    void organizerSeesTheirEventWithTheirRole() throws Exception {
        // guion
        UUID event = createEvent("Boda Ana y Luis", "2099-12-12");
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);

        // correr + revisar
        myEvents(organizer)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(event.toString()))
                .andExpect(jsonPath("$[0].name").value("Boda Ana y Luis"))
                .andExpect(jsonPath("$[0].eventDate").value("2099-12-12"))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andExpect(jsonPath("$[0].role").value("OWNER"));
    }

    @Test
    void eventsAreSortedByEventDateNewestFirst() throws Exception {
        // guion
        UUID user = createUser("ana@example.com");
        invite(createEvent("Medio", "2099-06-01"), user, OWNER, ACCEPTED);
        invite(createEvent("Viejo", "2099-01-01"), user, OWNER, ACCEPTED);
        invite(createEvent("Nuevo", "2099-12-01"), user, MODERATOR, ACCEPTED);

        // correr + revisar
        myEvents(user)
                .andExpect(jsonPath("$[*].name", contains("Nuevo", "Medio", "Viejo")))
                .andExpect(jsonPath("$[0].role").value("MODERATOR"));
    }

    @Test
    void userBNeverSeesTheEventsOfUserA() throws Exception {
        // guion
        UUID userA = createUser("ana@example.com");
        invite(createEvent("Evento de A", "2099-12-12"), userA, OWNER, ACCEPTED);
        UUID userB = createUser("beto@example.com");
        invite(createEvent("Evento de B", "2099-12-12"), userB, OWNER, ACCEPTED);

        // correr + revisar
        myEvents(userB)
                .andExpect(jsonPath("$[*].name", contains("Evento de B")));
    }

    @Test
    void pendingInvitationsAndDeletedEventsAreNotListed() throws Exception {
        // guion
        UUID user = createUser("ana@example.com");
        invite(createEvent("Pendiente", "2099-12-12"), user, MODERATOR, PENDING);
        UUID deleted = createEvent("Borrado", "2099-12-12");
        invite(deleted, user, OWNER, ACCEPTED);
        jdbc.sql("UPDATE events SET deleted_at = now() WHERE id = :id").param("id", deleted).update();

        // correr + revisar
        myEvents(user)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        mockMvc.perform(get("/me/events"))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions myEvents(UUID userId) throws Exception {
        return mockMvc.perform(get("/me/events").with(jwt().jwt(token -> token.subject(userId.toString()))));
    }

    private UUID createEvent(String name, String eventDate) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO events (id, name, color, public_code, event_date, images_per_user, status)
                VALUES (:id, :name, '#000000', :code, CAST(:eventDate AS DATE), 10, 'OPEN')
                """)
                .param("id", id).param("name", name).param("eventDate", eventDate)
                .param("code", id.toString().substring(0, 22))
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
