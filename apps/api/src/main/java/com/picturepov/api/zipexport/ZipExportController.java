package com.picturepov.api.zipexport;

import com.picturepov.api.common.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class ZipExportController {

    /** The signed-in user's id: the token's sub claim. */
    private static final String USER_ID = "T(java.util.UUID).fromString(subject)";

    private final ZipExportService zipExportService;

    ZipExportController(ZipExportService zipExportService) {
        this.zipExportService = zipExportService;
    }

    @PostMapping("/me/events/{id}/zip-export")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @ApiResponse(responseCode = "202", description = "The ZIP is generated in the background; the one already in progress if any, so no second generation starts")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "403", description = "Moderator of the event: the ZIP is organizer-only (RN-05)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "404", description = "No Accepted invitation to the event, or it does not exist or was deleted",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "409", description = "NO_PHOTOS_TO_EXPORT: the event has no processed photo",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    ZipExportResponse requestZipExport(@PathVariable UUID id, @AuthenticationPrincipal(expression = USER_ID) UUID userId) {
        return zipExportService.request(id, userId);
    }

    @GetMapping("/me/events/{id}/zip-export")
    @ApiResponse(responseCode = "200", description = "The event's latest ZIP, with a fresh download link when available")
    @ApiResponse(responseCode = "204", description = "No ZIP was requested for the event yet", content = @Content)
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "403", description = "Moderator of the event: the ZIP is organizer-only (RN-05)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "404", description = "No Accepted invitation to the event, or it does not exist or was deleted",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    ResponseEntity<ZipExportResponse> getZipExport(@PathVariable UUID id, @AuthenticationPrincipal(expression = USER_ID) UUID userId) {
        return zipExportService.latest(id, userId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
