package com.picturepov.api.event;

import com.picturepov.api.TestData;
import com.picturepov.api.invitation.InvitationRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.picturepov.api.invitation.InvitationRole.MODERATOR;
import static com.picturepov.api.invitation.InvitationRole.OWNER;
import static com.picturepov.api.invitation.InvitationStatus.ACCEPTED;
import static com.picturepov.api.invitation.InvitationStatus.PENDING;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

/** The queries behind access and listings: Accepted invitations only, soft-deleted events left out (RN-04, RN-06). */
@SpringBootTest
@Transactional
class EventRepositoryTest {

    @Autowired EventRepository eventRepository;
    @Autowired JdbcClient jdbc;

    TestData data;

    @BeforeEach
    void setUp() {
        data = new TestData(jdbc);
    }

    @Test
    void acceptedInvitationGivesTheUserTheirRole() {
        // Arrange
        UUID event = data.event();
        UUID moderator = data.user("beto@example.com");
        data.invite(event, moderator, MODERATOR, ACCEPTED);

        // Act
        Optional<InvitationRole> role = eventRepository.findAcceptedInvitationRole(event, moderator);

        // Assert
        assertThat(role).contains(MODERATOR);
    }

    @Test
    void pendingInvitationGivesNoRole() {
        // Arrange
        UUID event = data.event();
        UUID invitee = data.user("ana@example.com");
        data.invite(event, invitee, OWNER, PENDING);

        // Act
        Optional<InvitationRole> role = eventRepository.findAcceptedInvitationRole(event, invitee);

        // Assert
        assertThat(role).isEmpty();
    }

    @Test
    void organizerOfAnotherEventGetsNoRole() {
        // Arrange
        UUID eventOfA = data.event();
        data.invite(eventOfA, data.user("ana@example.com"), OWNER, ACCEPTED);
        UUID userB = data.user("beto@example.com");
        data.invite(data.event(), userB, OWNER, ACCEPTED);

        // Act
        Optional<InvitationRole> role = eventRepository.findAcceptedInvitationRole(eventOfA, userB);

        // Assert
        assertThat(role).isEmpty();
    }

    @Test
    void deletedEventGivesNoRole() {
        // Arrange
        UUID event = data.event();
        UUID organizer = data.user("ana@example.com");
        data.invite(event, organizer, OWNER, ACCEPTED);
        data.deleteEvent(event);

        // Act
        Optional<InvitationRole> role = eventRepository.findAcceptedInvitationRole(event, organizer);

        // Assert
        assertThat(role).isEmpty();
    }

    @Test
    void myEventsAreSortedNewestEventDateFirstWithMyRole() {
        // Arrange
        UUID user = data.user("ana@example.com");
        data.invite(data.event("Medio", "2099-06-01"), user, OWNER, ACCEPTED);
        data.invite(data.event("Viejo", "2099-01-01"), user, OWNER, ACCEPTED);
        data.invite(data.event("Nuevo", "2099-12-01"), user, MODERATOR, ACCEPTED);

        // Act
        List<MyEventResponse> events = eventRepository.findEventsWithAcceptedInvitation(user);

        // Assert
        assertThat(events).extracting(MyEventResponse::name, MyEventResponse::role)
                .containsExactly(tuple("Nuevo", MODERATOR), tuple("Medio", OWNER), tuple("Viejo", OWNER));
    }

    @Test
    void myEventsLeaveOutPendingInvitationsDeletedEventsAndOtherUsersEvents() {
        // Arrange
        UUID user = data.user("ana@example.com");
        data.invite(data.event("Pendiente", "2099-12-12"), user, MODERATOR, PENDING);
        UUID deleted = data.event("Borrado", "2099-12-12");
        data.invite(deleted, user, OWNER, ACCEPTED);
        data.deleteEvent(deleted);
        data.invite(data.event("De otro", "2099-12-12"), data.user("beto@example.com"), OWNER, ACCEPTED);

        // Act
        List<MyEventResponse> events = eventRepository.findEventsWithAcceptedInvitation(user);

        // Assert
        assertThat(events).isEmpty();
    }

    @Test
    void adminListShowsTheOrganizerEmailOrNullWithoutAnOwner() {
        // Arrange
        UUID withOwner = data.event("Con organizador", "2099-12-12");
        data.invite(withOwner, data.user("ana@example.com"), OWNER, ACCEPTED);
        UUID withoutOwner = data.event("Sin organizador", "2099-12-12");

        // Act
        List<AdminEventResponse> events = listAll(List.of(withOwner, withoutOwner));

        // Assert
        assertThat(events).extracting(AdminEventResponse::name, AdminEventResponse::organizerEmail)
                .containsExactlyInAnyOrder(tuple("Con organizador", "ana@example.com"), tuple("Sin organizador", null));
    }

    @Test
    void adminListIsSortedAsTheServiceAsks() {
        // Arrange
        UUID older = data.event("Viejo", "2099-01-10");
        UUID newer = data.event("Nuevo", "2099-12-31");

        // Act
        List<AdminEventResponse> events = listAll(List.of(older, newer));

        // Assert
        assertThat(events).extracting(AdminEventResponse::name).containsExactly("Nuevo", "Viejo");
    }

    @Test
    void adminListLeavesOutDeletedEvents() {
        // Arrange
        UUID deleted = data.event();
        data.deleteEvent(deleted);

        // Act
        List<AdminEventResponse> events = listAll(List.of(deleted));

        // Assert
        assertThat(events).isEmpty();
    }

    @Test
    void deletedEventCannotBeReachedByItsPublicCode() {
        // Arrange
        UUID deleted = data.event();
        data.deleteEvent(deleted);

        // Act
        Optional<Event> event = eventRepository.findByPublicCode(TestData.publicCode(deleted));

        // Assert
        assertThat(event).isEmpty();
    }

    /** The admin list with AdminEventService's sort, narrowed to these events so other rows do not matter. */
    private List<AdminEventResponse> listAll(List<UUID> ids) {
        Sort newestFirst = Sort.by(Sort.Order.desc("eventDate"), Sort.Order.desc("id"));
        return eventRepository.findAllWithOrganizerEmail(PageRequest.of(0, 1000, newestFirst))
                .filter(event -> ids.contains(event.id()))
                .toList();
    }
}
