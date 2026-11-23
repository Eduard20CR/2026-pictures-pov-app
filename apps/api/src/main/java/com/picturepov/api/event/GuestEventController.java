package com.picturepov.api.event;

import com.picturepov.api.common.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/events")
class GuestEventController {

    private final GuestEventService guestEventService;

    GuestEventController(GuestEventService guestEventService) {
        this.guestEventService = guestEventService;
    }

    @GetMapping("/{publicCode}")
    @ApiResponse(responseCode = "200", description = "Open event")
    @ApiResponse(responseCode = "404", description = "Event does not exist, is archived or was deleted",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "409", description = "Event is closed and no longer accepts photos",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    GuestEventResponse getGuestEvent(@PathVariable String publicCode) {
        return guestEventService.getGuestEvent(publicCode);
    }

}
