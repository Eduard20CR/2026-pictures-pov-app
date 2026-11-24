package com.picturepov.api.image;

import com.jayway.jsonpath.JsonPath;
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

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static com.picturepov.api.image.ReviewImagesController.MAX_LIMIT;
import static com.picturepov.api.invitation.InvitationRole.MODERATOR;
import static com.picturepov.api.invitation.InvitationRole.OWNER;
import static com.picturepov.api.invitation.InvitationStatus.ACCEPTED;
import static com.picturepov.api.invitation.InvitationStatus.PENDING;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReviewImagesControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcClient jdbc;

    @Test
    void organizerSeesEveryPhotoNewestFirst() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        UUID older = addPhoto(event, "2099-01-01T10:00:00Z", true);
        UUID newer = addPhoto(event, "2099-01-01T11:00:00Z", true);

        // correr + revisar
        listImages(event, organizer, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", contains(newer.toString(), older.toString())))
                .andExpect(jsonPath("$.items[0].createdAt").value("2099-01-01T11:00:00Z"))
                .andExpect(jsonPath("$.items[0].thumbnailUrl").value(containsString("thumbs/" + event + "/" + newer + ".webp")))
                .andExpect(jsonPath("$.items[0].thumbnailUrl").value(containsString("X-Amz-Signature=")))
                .andExpect(jsonPath("$.nextCursor").isEmpty());
    }

    @Test
    void photoWithoutThumbnailYetHasNoThumbnailUrl() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        addPhoto(event, "2099-01-01T10:00:00Z", false);

        // correr + revisar
        listImages(event, organizer, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].thumbnailUrl").isEmpty());
    }

    @Test
    void showsVisibleAndHiddenPhotosAndSaysWhichAreHidden() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        UUID visible = addPhoto(event, "2099-01-01T10:00:00Z", true);
        UUID hidden = addPhoto(event, "2099-01-01T11:00:00Z", true);
        hide(hidden);

        // correr + revisar
        listImages(event, organizer, "")
                .andExpect(jsonPath("$.items[*].id", contains(hidden.toString(), visible.toString())))
                .andExpect(jsonPath("$.items[*].hidden", contains(true, false)));
    }

    @Test
    void pagesThroughThePhotosWithACursorWithoutRepeatingOrSkipping() throws Exception {
        // guion: the two newest share their instant, so the cursor must break ties by id
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        UUID oldest = addPhoto(event, "2099-01-01T09:00:00Z", true);
        UUID tieA = addPhoto(event, "2099-01-01T10:00:00Z", true);
        UUID tieB = addPhoto(event, "2099-01-01T10:00:00Z", true);
        List<String> ties = Stream.of(tieA, tieB).map(UUID::toString).sorted(Comparator.reverseOrder()).toList();
        String first = ties.get(0);
        String second = ties.get(1);

        // correr + revisar
        String body = listImages(event, organizer, "?limit=1")
                .andExpect(jsonPath("$.items[*].id", contains(first)))
                .andExpect(jsonPath("$.nextCursor").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        String cursor = JsonPath.read(body, "$.nextCursor");

        body = listImages(event, organizer, "?limit=1&cursor=" + cursor)
                .andExpect(jsonPath("$.items[*].id", contains(second)))
                .andReturn().getResponse().getContentAsString();
        cursor = JsonPath.read(body, "$.nextCursor");

        listImages(event, organizer, "?limit=2&cursor=" + cursor)
                .andExpect(jsonPath("$.items[*].id", contains(oldest.toString())))
                .andExpect(jsonPath("$.nextCursor").isEmpty());
    }

    @Test
    void limitOutsideOneToHundredOrAMalformedCursorGets400() throws Exception {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);

        // correr + revisar
        listImages(event, organizer, "?limit=0").andExpect(status().isBadRequest());
        listImages(event, organizer, "?limit=" + (MAX_LIMIT + 1)).andExpect(status().isBadRequest());
        listImages(event, organizer, "?cursor=not-a-cursor").andExpect(status().isBadRequest());
    }

    @Test
    void moderatorSeesThePhotosAsTheOrganizerDoes() throws Exception {
        // guion
        UUID event = createEvent();
        UUID moderator = createUser("beto@example.com");
        invite(event, moderator, MODERATOR, ACCEPTED);
        UUID photo = addPhoto(event, "2099-01-01T10:00:00Z", true);

        // correr + revisar
        listImages(event, moderator, "")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[*].id", contains(photo.toString())));
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404() throws Exception {
        // guion
        UUID event = createEvent();
        addPhoto(event, "2099-01-01T10:00:00Z", true);
        UUID pending = createUser("ana@example.com");
        invite(event, pending, MODERATOR, PENDING);
        UUID stranger = createUser("beto@example.com");

        // correr + revisar
        listImages(event, pending, "")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
        listImages(event, stranger, "")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("EVENT_NOT_FOUND"));
    }

    @Test
    void anonymousCallerGets401() throws Exception {
        mockMvc.perform(get("/me/events/{id}/images", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    private ResultActions listImages(UUID eventId, UUID userId, String query) throws Exception {
        return mockMvc.perform(get("/me/events/{id}/images" + query, eventId)
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

    /** A photo uploaded at createdAt; ready means its thumbnail exists. */
    private UUID addPhoto(UUID eventId, String createdAt, boolean ready) {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO images (id, event_id, s3_key, thumb_key, content_type, size_bytes, guest_id, status, created_at)
                VALUES (:id, :eventId, :s3Key, :thumbKey, 'image/jpeg', 1000, :guestId, :status, CAST(:createdAt AS timestamptz))
                """)
                .param("id", id).param("eventId", eventId)
                .param("s3Key", "originals/" + eventId + "/" + id)
                .param("thumbKey", ready ? "thumbs/" + eventId + "/" + id + ".webp" : null)
                .param("guestId", UUID.randomUUID())
                .param("status", ready ? "READY" : "PENDING")
                .param("createdAt", createdAt)
                .update();
        return id;
    }
}
