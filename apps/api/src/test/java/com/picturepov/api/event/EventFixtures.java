package com.picturepov.api.event;

import com.picturepov.api.invitation.InvitationRole;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

/**
 * Real event objects for unit tests in other packages, where Event's factory and EventRepository are not visible.
 * Only the database is replaced: the services built here run their real rules.
 */
public final class EventFixtures {

    public static final String PUBLIC_CODE = "bodaAnaYLuis0000000000";

    private EventFixtures() {}

    public static Event openEvent(int imagesPerUser) {
        return event(EventStatus.OPEN, imagesPerUser);
    }

    public static Event event(EventStatus status, int imagesPerUser) {
        Event event = Event.createOpen("Boda Ana y Luis", LocalDate.of(2099, 12, 12), PUBLIC_CODE);
        ReflectionTestUtils.setField(event, "status", status);
        ReflectionTestUtils.setField(event, "imagesPerUser", imagesPerUser);
        return event;
    }

    /** A GuestEventService whose database holds only this event. */
    public static GuestEventService guestEventService(Event event) {
        EventRepository repository = lenientMock();
        when(repository.findByPublicCode(event.getPublicCode())).thenReturn(Optional.of(event));
        return new GuestEventService(repository);
    }

    /** An EventAccess where only this user holds an Accepted invitation to this event, with this role. */
    public static EventAccess eventAccess(UUID eventId, UUID userId, InvitationRole role) {
        EventRepository repository = lenientMock();
        when(repository.findAcceptedInvitationRole(eventId, userId)).thenReturn(Optional.of(role));
        return new EventAccess(repository);
    }

    private static EventRepository lenientMock() {
        return mock(EventRepository.class, withSettings().strictness(Strictness.LENIENT));
    }
}
