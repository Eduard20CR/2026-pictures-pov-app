package com.picturepov.api.event;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/events")
class AdminEventController {

    private final EventService eventService;

    AdminEventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Event created in DRAFT and assigned to the owner email")
    @ApiResponse(responseCode = "400", description = "Invalid or missing fields", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Not signed in", content = @Content)
    @ApiResponse(responseCode = "403", description = "Not an administrator", content = @Content)
    AdminEventResponse create(@Valid @RequestBody CreateEventRequest request) {
        return eventService.create(request);
    }


    @GetMapping
    @ApiResponse(responseCode = "200", description = "All events, newest event date first")
    @ApiResponse(responseCode = "400", description = "Invalid page or size", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "401", description = "Not signed in", content = @Content)
    @ApiResponse(responseCode = "403", description = "Not an administrator", content = @Content)
    AdminEventPage list(@RequestParam(defaultValue = "0") @Min(0) int page, @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return eventService.list(page, size);
    }
}