package com.picturepov.api.event;

import com.picturepov.api.common.error.ApiProblem;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.invitation.InvitationRole;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
class MyEventsController {

    private final EventRepository eventRepository;
    private final EventAccess eventAccess;

    MyEventsController(EventRepository eventRepository, EventAccess eventAccess) {
        this.eventRepository = eventRepository;
        this.eventAccess = eventAccess;
    }

    /** Filters by the token's user, so it needs no EventAccess check (RN-04). */
    @GetMapping("/me/events")
    @Transactional(readOnly = true)
    @ApiResponse(responseCode = "200", description = "Events where the signed-in user holds an Accepted invitation, newest event date first")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    List<MyEventResponse> myEvents(@AuthenticationPrincipal Jwt jwt) {
        return eventRepository.findMyEvents(userId(jwt));
    }

    @GetMapping("/me/events/{id}")
    @Transactional(readOnly = true)
    @ApiResponse(responseCode = "200", description = "The event, with the signed-in user's role")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "404", description = "No Accepted invitation to the event, or it does not exist or was deleted",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    MyEventResponse myEvent(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        InvitationRole role = eventAccess.roleIn(id, userId(jwt));
        // Still 404 if the event is deleted between the two reads.
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.EVENT_NOT_FOUND, "Event not found"));
        return new MyEventResponse(event.getId(), event.getPublicCode(), event.getName(),
                event.getEventDate(), event.getStatus(), role);
    }

    private static UUID userId(Jwt jwt) {
        return UUID.fromString(Objects.requireNonNull(jwt.getSubject()));
    }
}
