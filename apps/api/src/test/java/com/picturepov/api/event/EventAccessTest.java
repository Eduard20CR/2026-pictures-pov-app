package com.picturepov.api.event;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.invitation.InvitationRole;
import com.picturepov.api.invitation.InvitationStatus;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.picturepov.api.invitation.InvitationRole.MODERATOR;
import static com.picturepov.api.invitation.InvitationRole.OWNER;
import static com.picturepov.api.invitation.InvitationStatus.ACCEPTED;
import static com.picturepov.api.invitation.InvitationStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EventAccessTest {

    @Autowired EventAccess eventAccess;
    @Autowired JdbcClient jdbc;

    @Test
    void organizerReachesTheirEventAsOwner() {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);

        // correr + revisar
        assertThat(eventAccess.roleIn(event, organizer)).isEqualTo(InvitationRole.OWNER);
    }

    @Test
    void organizerOfAnotherEventGets404() {
        // guion
        UUID eventOfA = createEvent();
        UUID userA = createUser("ana@example.com");
        invite(eventOfA, userA, OWNER, ACCEPTED);
        UUID eventOfB = createEvent();
        UUID userB = createUser("beto@example.com");
        invite(eventOfB, userB, OWNER, ACCEPTED);

        // correr + revisar
        assertNotFound(() -> eventAccess.roleIn(eventOfA, userB));
    }

    @Test
    void pendingInvitationGets404() {
        // guion
        UUID event = createEvent();
        UUID invitee = createUser("ana@example.com");
        invite(event, invitee, OWNER, PENDING);

        // correr + revisar
        assertNotFound(() -> eventAccess.roleIn(event, invitee));
    }

    @Test
    void deletedEventGets404() {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);
        jdbc.sql("UPDATE events SET deleted_at = now() WHERE id = :id").param("id", event).update();

        // correr + revisar
        assertNotFound(() -> eventAccess.roleIn(event, organizer));
    }

    @Test
    void moderatorIsRefusedAnOrganizerOnlyAction() {
        // guion
        UUID event = createEvent();
        UUID moderator = createUser("mod@example.com");
        invite(event, moderator, MODERATOR, ACCEPTED);

        // correr + revisar
        assertThatThrownBy(() -> eventAccess.requireOrganizer(event, moderator))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void organizerMayDoAnOrganizerOnlyAction() {
        // guion
        UUID event = createEvent();
        UUID organizer = createUser("ana@example.com");
        invite(event, organizer, OWNER, ACCEPTED);

        // correr + revisar
        assertThatCode(() -> eventAccess.requireOrganizer(event, organizer)).doesNotThrowAnyException();
    }

    private static void assertNotFound(ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(NotFoundException.class)
                .extracting(e -> ((NotFoundException) e).getCode())
                .isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    private UUID createEvent() {
        UUID id = UUID.randomUUID();
        jdbc.sql("""
                INSERT INTO events (id, name, color, public_code, event_date, images_per_user, status)
                VALUES (:id, 'Test event', '#000000', :code, DATE '2099-12-12', 10, 'OPEN')
                """)
                .param("id", id)
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
