package com.picturepov.api.event;

import com.picturepov.api.common.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/events")
class AdminEventController {

    private final AdminEventService adminEventService;

    AdminEventController(AdminEventService adminEventService) {
        this.adminEventService = adminEventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Event created Open, with an OWNER invitation for the organizer email")
    @ApiResponse(responseCode = "400", description = "Invalid or missing fields", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "401", description = "Not signed in", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "403", description = "Not an administrator", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    AdminEventResponse createEvent(@Valid @RequestBody CreateEventRequest request) {
        return adminEventService.createEvent(request);
    }


    @GetMapping
    @ApiResponse(responseCode = "200", description = "All events, newest event date first")
    @ApiResponse(responseCode = "400", description = "Invalid page or size", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "401", description = "Not signed in", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "403", description = "Not an administrator", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    AdminEventPage listAllEvents(@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return adminEventService.listAllEvents(page, size);
    }
}