package com.picturepov.api.image;

import com.picturepov.api.common.error.ApiProblem;
import com.picturepov.api.guest.GuestIdFilter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
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
    @ApiResponse(responseCode = "201", description = "Upload reserved; PUT the photo once to uploadUrl with If-None-Match set to * (412 means it is already stored)")
    @ApiResponse(responseCode = "400", description = "Unsupported type, invalid size or malformed body", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "404", description = "Event does not exist, is archived or was deleted", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "409", description = "Upload limit reached, or the event is closed", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    UploadResponse createUpload(@PathVariable String publicCode, @RequestAttribute(GuestIdFilter.ATTRIBUTE) UUID guestId, @RequestBody UploadRequest request) {
        return imageUploadService.createUpload(publicCode, guestId, request);
    }
}
