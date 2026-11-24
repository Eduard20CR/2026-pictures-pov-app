package com.picturepov.api.image;

import com.picturepov.api.common.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class ImageDownloadController {

    /** The signed-in user's id: the token's sub claim. */
    private static final String USER_ID = "T(java.util.UUID).fromString(subject)";

    private final ImageDownloadService imageDownloadService;

    ImageDownloadController(ImageDownloadService imageDownloadService) {
        this.imageDownloadService = imageDownloadService;
    }

    @GetMapping("/me/events/{id}/images/{imageId}/download")
    @ApiResponse(responseCode = "200", description = "A temporary signed link that downloads the photo's original file")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "403", description = "Moderator of the event: downloading is organizer-only (RN-05)",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "404", description = "No Accepted invitation to the event, it does not exist or was deleted, or the photo is not the event's or is still processing",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    ImageDownloadResponse downloadLink(@PathVariable UUID id,
                                       @PathVariable UUID imageId,
                                       @AuthenticationPrincipal(expression = USER_ID) UUID userId) {
        return imageDownloadService.downloadLink(id, imageId, userId);
    }
}
