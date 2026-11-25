package com.picturepov.api.zipexport;

import com.picturepov.api.invitation.InvitationRole;
import com.picturepov.api.invitation.InvitationStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.picturepov.api.invitation.InvitationRole.MODERATOR;
import static com.picturepov.api.invitation.InvitationRole.OWNER;
import static com.picturepov.api.invitation.InvitationStatus.ACCEPTED;
import static com.picturepov.api.invitation.InvitationStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ZipExportControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcClient jdbc;
    @MockitoBean ZipExportQueue queue;

    @Test
    void organizerRequestsTheZipAndItIsQueuedInProgress() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        addPhoto(event);

        // correr
        request(event, organizer)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // revisar
        UUID exportId = latestExportId(event);
        verify(queue).send(exportId);
        getStatus(event, organizer)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void requestingAgainWhileInProgressStartsNoSecondGeneration() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        addPhoto(event);
        request(event, organizer).andExpect(status().isAccepted());
        UUID first = latestExportId(event);
        markRunning(first);

        // correr
        request(event, organizer)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));

        // revisar
        assertThat(latestExportId(event)).isEqualTo(first);
        verify(queue, times(1)).send(any());
    }

    @Test
    void exportStillRunningAfterEveryRetryIsFailedAndANewRequestStartsAgain() throws Exception {
        // guion: the worker timed out on its last receive (3 × 16 min), so nothing marked it FAILED
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        addPhoto(event);
        UUID stuck = addExport(event, organizer, "RUNNING", "now() - interval '49 minutes'");

        // correr + revisar
        getStatus(event, organizer)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
        request(event, organizer)
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        assertThat(latestExportId(event)).isNotEqualTo(stuck);
        verify(queue).send(latestExportId(event));
    }

    @Test
    void exportRunningForLessThanEveryRetryIsStillInProgress() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        addExport(event, organizer, "RUNNING", "now() - interval '47 minutes'");

        // correr + revisar
        getStatus(event, organizer)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void availableZipComesWithASignedLinkValidForP07() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        UUID export = addExport(event, organizer, "READY", "now()");
        jdbc.sql("UPDATE zip_exports SET s3_key = :key, image_count = 12, size_bytes = 3400000, completed_at = now() WHERE id = :id")
                .param("key", "zips/" + export + ".zip").param("id", export).update();

        // correr + revisar
        getStatus(event, organizer)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.imageCount").value(12))
                .andExpect(jsonPath("$.sizeBytes").value(3400000))
                .andExpect(jsonPath("$.downloadUrl").value(allOf(
                        containsString("zips/" + export + ".zip"),
                        containsString("X-Amz-Signature="),
                        containsString("X-Amz-Expires=86400"),
                        containsString("response-content-disposition=attachment"))));
    }

    @Test
    void failedZipHasNoLinkAndCanBeRequestedAgain() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        addPhoto(event);
        UUID failed = addExport(event, organizer, "FAILED", "now()");

        // correr + revisar
        getStatus(event, organizer)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"))
                .andExpect(jsonPath("$.downloadUrl").doesNotExist());
        request(event, organizer).andExpect(status().isAccepted());
        assertThat(latestExportId(event)).isNotEqualTo(failed);
    }

    @Test
    void eventWithoutProcessedPhotosGets409() throws Exception {
        // guion: a photo still processing is not in the ZIP
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        UUID photo = addPhoto(event);
        jdbc.sql("UPDATE images SET status = 'PENDING' WHERE id = :id").param("id", photo).update();

        // correr + revisar
        request(event, organizer)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("NO_PHOTOS_TO_EXPORT"));
        verifyNoInteractions(queue);
    }

    @Test
    void zipThatCouldNotBeQueuedIsFailed() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        addPhoto(event);
        doThrow(new IllegalStateException("SQS unavailable")).when(queue).send(any());

        // correr + revisar
        request(event, organizer).andExpect(status().isInternalServerError());
        getStatus(event, organizer)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FAILED"));
    }

    @Test
    void eventWithoutAZipGets204() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);

        // correr + revisar
        getStatus(event, organizer).andExpect(status().isNoContent());
    }

    @Test
    void moderatorGets403() throws Exception {
        // guion
        UUID event = createEvent();
        UUID moderator = createUser("beto@example.com");
        invite(event, moderator, MODERATOR, ACCEPTED);
        addPhoto(event);

        // correr + revisar
        request(event, moderator)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        getStatus(event, moderator)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        verifyNoInteractions(queue);
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404() throws Exception {
        // guion
        UUID event = createEvent();
        addPhoto(event);
        UUID pending = createUser("ana@example.com");
        invite(event, pending, OWNER, PENDING);
        UUID stranger = createUser("beto@example.com");

        // correr + revisar
        for (UUID user : new UUID[]{pending, stranger}) {
            request(event, user)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
            getStatus(event, user)
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
        }
        verifyNoInteractions(queue);
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        mockMvc.perform(post("/me/events/{id}/zip-export", UUID.randomUUID())).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/me/events/{id}/zip-export", UUID.randomUUID())).andExpect(status().isUnauthorized());
    }

    private UUID addExport(UUID eventId, UUID userId, String status, String createdAt) {
        UUID id = UUID.randomUUID();
        jdbc.sql("INSERT INTO zip_exports (id, event_id, requested_by, status, created_at) VALUES (:id, :eventId, :userId, :status, " + createdAt + ")")
                .param("id", id).param("eventId", eventId).param("userId", userId).param("status", status)
                .update();
        return id;
    }

    private void markRunning(UUID exportId) {
        jdbc.sql("UPDATE zip_exports SET status = 'RUNNING' WHERE id = :id").param("id", exportId).update();
    }

    private ResultActions request(UUID eventId, UUID userId) throws Exception {
        return mockMvc.perform(post("/me/events/{id}/zip-export", eventId)
                .with(jwt().jwt(token -> token.subject(userId.toString()))));
    }

    private ResultActions getStatus(UUID eventId, UUID userId) throws Exception {
        return mockMvc.perform(get("/me/events/{id}/zip-export", eventId)
                .with(jwt().jwt(token -> token.subject(userId.toString()))));
    }

    private UUID latestExportId(UUID eventId) {
        return jdbc.sql("SELECT id FROM zip_exports WHERE event_id = :eventId ORDER BY created_at DESC LIMIT 1")
                .param("eventId", eventId).query(UUID.class).single();
    }

    private UUID createEvent() {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO events (id, name, color, public_code, event_date, images_per_user, status)
                VALUES (:id, 'Boda', '#000000', :code, DATE '2099-12-12', 10, 'OPEN')
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

    private void invite(UUID eventId, UUID userId, InvitationRole role, InvitationStatus status) {
        jdbc.sql("""
                INSERT INTO invitations (id, event_id, email, user_id, role, status)
                SELECT :id, :eventId, email, id, :role, :status FROM users WHERE id = :userId
                """)
                .param("id", UUID.randomUUID()).param("eventId", eventId).param("userId", userId)
                .param("role", role.name()).param("status", status.name())
                .update();
    }

    /** A processed JPEG photo of the event. */
    private UUID addPhoto(UUID eventId) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO images (id, event_id, s3_key, thumb_key, content_type, size_bytes, guest_id, status)
                VALUES (:id, :eventId, :s3Key, :thumbKey, 'image/jpeg', 1000, :guestId, 'READY')
                """)
                .param("id", id).param("eventId", eventId)
                .param("s3Key", "originals/" + eventId + "/" + id)
                .param("thumbKey", "thumbs/" + eventId + "/" + id + ".webp")
                .param("guestId", UUID.randomUUID())
                .update();
        return id;
    }
}
