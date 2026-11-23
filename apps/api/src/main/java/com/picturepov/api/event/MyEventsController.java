package com.picturepov.api.event;

import com.picturepov.api.common.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
class MyEventsController {

    /** The signed-in user's id: the token's sub claim. */
    private static final String USER_ID = "T(java.util.UUID).fromString(subject)";

    private final MyEventsService myEventsService;

    MyEventsController(MyEventsService myEventsService) {
        this.myEventsService = myEventsService;
    }

    @GetMapping("/me/events")
    @ApiResponse(responseCode = "200", description = "Events where the signed-in user holds an Accepted invitation, newest event date first")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    List<MyEventResponse> listMyEvents(@AuthenticationPrincipal(expression = USER_ID) UUID userId) {
        return myEventsService.listMyEvents(userId);
    }

    @GetMapping("/me/events/{id}")
    @ApiResponse(responseCode = "200", description = "The event, with the signed-in user's role")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "404", description = "No Accepted invitation to the event, or it does not exist or was deleted",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    MyEventResponse getMyEvent(@PathVariable UUID id, @AuthenticationPrincipal(expression = USER_ID) UUID userId) {
        return myEventsService.getMyEvent(id, userId);
    }
}
