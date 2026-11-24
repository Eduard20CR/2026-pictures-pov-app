package com.picturepov.api.image;

import com.picturepov.api.common.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class ReviewImagesController {

    /** The signed-in user's id: the token's sub claim. */
    private static final String USER_ID = "T(java.util.UUID).fromString(subject)";

    /** Largest page of photos one request may ask for. */
    static final int MAX_LIMIT = 100;

    private final ReviewImagesService reviewImagesService;

    ReviewImagesController(ReviewImagesService reviewImagesService) {
        this.reviewImagesService = reviewImagesService;
    }

    @GetMapping("/me/events/{id}/images")
    @ApiResponse(responseCode = "200", description = "The review: every photo of the event, hidden ones included, newest first")
    @ApiResponse(responseCode = "400", description = "Invalid limit or malformed cursor",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "404", description = "No Accepted invitation to the event, or it does not exist or was deleted",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    ReviewImagePage listImages(@PathVariable UUID id,
                               @RequestParam(defaultValue = "30") @Min(1) @Max(MAX_LIMIT) int limit,
                               @RequestParam(required = false) String cursor,
                               @AuthenticationPrincipal(expression = USER_ID) UUID userId) {
        return reviewImagesService.listImages(id, userId, limit, cursor);
    }
}
