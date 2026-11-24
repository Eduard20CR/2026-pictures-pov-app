package com.picturepov.api.image;

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

import java.net.URLEncoder;
import java.util.UUID;

import static com.picturepov.api.invitation.InvitationRole.MODERATOR;
import static com.picturepov.api.invitation.InvitationRole.OWNER;
import static com.picturepov.api.invitation.InvitationStatus.ACCEPTED;
import static com.picturepov.api.invitation.InvitationStatus.PENDING;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ImageDownloadControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcClient jdbc;

    @Test
    void organizerGetsASignedLinkThatDownloadsTheOriginalEvenOfAHiddenPhoto() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        UUID photo = addPhoto(event);
        hide(photo);

        // correr + revisar
        download(event, photo, organizer)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value(allOf(
                        containsString("originals/" + event + "/" + photo),
                        containsString("X-Amz-Signature="),
                        containsString("X-Amz-Expires=900"),
                        containsString("response-content-disposition=" + encode("attachment; filename=\"" + photo + ".jpg\"")))));
    }

    @Test
    void moderatorGets403() throws Exception {
        // guion
        UUID event = createEvent();
        UUID moderator = createUser("beto@example.com");
        invite(event, moderator, MODERATOR, ACCEPTED);
        UUID photo = addPhoto(event);

        // correr + revisar
        download(event, photo, moderator)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
    }

    @Test
    void photoThatDoesNotExistOrIsFromAnotherEventGets404() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        UUID otherEventPhoto = addPhoto(createEvent());

        // correr + revisar
        download(event, UUID.randomUUID(), organizer)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("IMAGE_NOT_FOUND"));
        download(event, otherEventPhoto, organizer)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("IMAGE_NOT_FOUND"));
    }

    @Test
    void photoStillProcessingGets404() throws Exception {
        // guion: its original may not be stored yet
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        UUID photo = addPhoto(event);
        jdbc.sql("UPDATE images SET status = 'PENDING', thumb_key = NULL WHERE id = :id").param("id", photo).update();

        // correr + revisar
        download(event, photo, organizer)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("IMAGE_NOT_FOUND"));
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404() throws Exception {
        // guion
        UUID event = createEvent();
        UUID photo = addPhoto(event);
        UUID pending = createUser("ana@example.com");
        invite(event, pending, OWNER, PENDING);
        UUID stranger = createUser("beto@example.com");

        // correr + revisar
        download(event, photo, pending)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
        download(event, photo, stranger)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        mockMvc.perform(get("/me/events/{id}/images/{imageId}/download", UUID.randomUUID(), UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    /** As the AWS signer encodes query values. */
    private static String encode(String value) {
        return URLEncoder.encode(value, UTF_8).replace("+", "%20");
    }

    private ResultActions download(UUID eventId, UUID imageId, UUID userId) throws Exception {
        return mockMvc.perform(get("/me/events/{id}/images/{imageId}/download", eventId, imageId)
                .with(jwt().jwt(token -> token.subject(userId.toString()))));
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

    private void hide(UUID imageId) {
        jdbc.sql("UPDATE images SET hidden = true WHERE id = :id").param("id", imageId).update();
    }

    /** A JPEG photo of the event, with its thumbnail generated. */
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
