package com.picturepov.api.invitation;

import com.picturepov.api.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/** The queries behind invitations: one per event and email, Pending only, deleted events left out. */
@SpringBootTest
@Transactional
class InvitationRepositoryTest {

    @Autowired InvitationRepository sut;
    @Autowired JdbcClient jdbc;

    TestData data;

    @BeforeEach
    void setUp() {
        data = new TestData(jdbc);
    }

    @Test
    void secondInvitationForTheSameEventAndEmailIsRefusedByTheDatabase() {
        // Arrange: InvitationService turns this into 409 when two requests race
        UUID event = data.event();
        sut.saveAndFlush(Invitation.createModerator(event, "beto@example.com"));

        // Act
        Throwable thrown = catchThrowable(() ->
                sut.saveAndFlush(Invitation.createModerator(event, "beto@example.com")));

        // Assert
        assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void anInvitationExistsOnlyForItsOwnEvent() {
        // Arrange
        UUID event = data.event();
        data.pendingInvitation(event, "beto@example.com");

        // Act
        boolean sameEvent = sut.existsByEventIdAndEmail(event, "beto@example.com");
        boolean otherEvent = sut.existsByEventIdAndEmail(data.event(), "beto@example.com");

        // Assert
        assertThat(sameEvent).isTrue();
        assertThat(otherEvent).isFalse();
    }

    @Test
    void pendingInvitationIsFoundById() {
        // Arrange
        UUID invitation = data.pendingInvitation(data.event(), "beto@example.com");

        // Act
        Optional<Invitation> found = sut.findPendingById(invitation);

        // Assert
        assertThat(found).map(Invitation::getEmail).contains("beto@example.com");
    }

    @Test
    void acceptedInvitationIsNoLongerPending() {
        // Arrange
        UUID invitation = data.invite(data.event(), data.user("beto@example.com"), InvitationRole.MODERATOR, InvitationStatus.ACCEPTED);

        // Act
        Optional<Invitation> found = sut.findPendingById(invitation);

        // Assert
        assertThat(found).isEmpty();
    }

    @Test
    void invitationToADeletedEventIsLeftOut() {
        // Arrange
        UUID event = data.event();
        UUID invitation = data.pendingInvitation(event, "beto@example.com");
        data.deleteEvent(event);

        // Act
        Optional<Invitation> byId = sut.findPendingById(invitation);
        List<PendingInvitationResponse> byEmail = sut.findPendingByEmail("beto@example.com");

        // Assert
        assertThat(byId).isEmpty();
        assertThat(byEmail).isEmpty();
    }

    @Test
    void pendingInvitationsOfAnEmailComeWithTheEventName() {
        // Arrange
        UUID event = data.event("Boda Ana y Luis", "2099-12-12");
        UUID invitation = data.pendingInvitation(event, "beto@example.com");
        data.pendingInvitation(data.event("Otra boda", "2099-12-12"), "carla@example.com");
        data.invite(data.event("Aceptada", "2099-12-12"), data.user("beto@example.com"), InvitationRole.MODERATOR, InvitationStatus.ACCEPTED);

        // Act
        List<PendingInvitationResponse> pending = sut.findPendingByEmail("beto@example.com");

        // Assert
        assertThat(pending).containsExactly(
                new PendingInvitationResponse(invitation, event, "Boda Ana y Luis", InvitationRole.MODERATOR));
    }
}
