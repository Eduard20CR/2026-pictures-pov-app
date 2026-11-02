package com.picturepov.api.image;

import com.picturepov.api.guest.GuestIdFilter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/events")
class ImageController {

    private final ImageUploadService imageUploadService;

    ImageController(ImageUploadService imageUploadService) {
        this.imageUploadService = imageUploadService;
    }

    @PostMapping("/{publicCode}/uploads")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Upload reserved; PUT the photo to uploadUrl")
    @ApiResponse(responseCode = "400", description = "Unsupported type or invalid size", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "404", description = "Event does not exist or is not active", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(responseCode = "409", description = "Upload limit reached", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    UploadResponse createUpload(@PathVariable String publicCode, @RequestAttribute(GuestIdFilter.ATTRIBUTE) UUID guestId, @RequestBody UploadRequest request) {
        return imageUploadService.createUpload(publicCode, guestId, request);
    }
}
