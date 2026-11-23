package com.picturepov.api.event;

import com.picturepov.api.common.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
class MyEventsController {

    private final EventRepository eventRepository;

    MyEventsController(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    /** Filters by the token's user, so it needs no EventAccess check (RN-04). */
    @GetMapping("/me/events")
    @Transactional(readOnly = true)
    @ApiResponse(responseCode = "200", description = "Events where the signed-in user holds an Accepted invitation, newest event date first")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    List<MyEventResponse> myEvents(@AuthenticationPrincipal Jwt jwt) {
        return eventRepository.findMyEvents(UUID.fromString(jwt.getSubject()));
    }
}
