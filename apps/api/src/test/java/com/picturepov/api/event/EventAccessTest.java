package com.picturepov.api.event;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.invitation.InvitationRole;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@Transactional
class EventAccessTest {

    @Autowired EventAccess eventAccess;
    @Autowired JdbcClient jdbc;

    UUID eventId = UUID.randomUUID();

    @BeforeEach
    void createEvent() {
        jdbc.sql("""
                INSERT INTO events (id, name, color, public_code, event_date, images_per_user, status)
                VALUES (:id, 'Test event', '#000000', :code, DATE '2099-12-12', 10, 'OPEN')
                """)
                .param("id", eventId)
                .param("code", eventId.toString().substring(0, 22))
                .update();
    }

    @Test
    void organizerReachesTheirEventAsOwner() {
        // guion
        UUID organizer = invite("ana@example.com", "OWNER", "ACCEPTED");

        // correr + revisar
        assertThat(eventAccess.roleIn(eventId, organizer)).isEqualTo(InvitationRole.OWNER);
    }

    @Test
    void userInvitedElsewhereGets404() {
        // guion
        invite("ana@example.com", "OWNER", "ACCEPTED");
        UUID otherUser = UUID.randomUUID();

        // correr + revisar
        assertNotFound(() -> eventAccess.roleIn(eventId, otherUser));
    }

    @Test
    void pendingInvitationGets404() {
        // guion
        UUID invitee = invite("ana@example.com", "OWNER", "PENDING");

        // correr + revisar
        assertNotFound(() -> eventAccess.roleIn(eventId, invitee));
    }

    @Test
    void deletedEventGets404() {
        // guion
        UUID organizer = invite("ana@example.com", "OWNER", "ACCEPTED");
        jdbc.sql("UPDATE events SET deleted_at = now() WHERE id = :id").param("id", eventId).update();

        // correr + revisar
        assertNotFound(() -> eventAccess.roleIn(eventId, organizer));
    }

    @Test
    void moderatorIsRefusedAnOrganizerOnlyAction() {
        // guion
        UUID moderator = invite("mod@example.com", "MODERATOR", "ACCEPTED");

        // correr + revisar
        assertThatThrownBy(() -> eventAccess.requireOrganizer(eventId, moderator))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void organizerMayDoAnOrganizerOnlyAction() {
        // guion
        UUID organizer = invite("ana@example.com", "OWNER", "ACCEPTED");

        // correr + revisar
        assertThatCode(() -> eventAccess.requireOrganizer(eventId, organizer)).doesNotThrowAnyException();
    }

    private static void assertNotFound(ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOf(NotFoundException.class)
                .extracting(e -> ((NotFoundException) e).getCode())
                .isEqualTo(ErrorCode.EVENT_NOT_FOUND);
    }

    /** Creates a user and an invitation linked to them; returns the user id. */
    private UUID invite(String email, String role, String status) {
        UUID userId = UUID.randomUUID();
        jdbc.sql("INSERT INTO users (id, email) VALUES (:id, :email)")
                .param("id", userId).param("email", email).update();
        jdbc.sql("""
                INSERT INTO invitations (id, event_id, email, user_id, role, status)
                VALUES (:id, :eventId, :email, :userId, :role, :status)
                """)
                .param("id", UUID.randomUUID()).param("eventId", eventId).param("email", email)
                .param("userId", userId).param("role", role).param("status", status)
                .update();
        return userId;
    }
}
