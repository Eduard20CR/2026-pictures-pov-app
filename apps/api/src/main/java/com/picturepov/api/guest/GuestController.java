package com.picturepov.api.guest;

import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/events")
class GuestController {
    private final GuestLimitService guestLimitService;

    GuestController(GuestLimitService guestLimitService) {
        this.guestLimitService = guestLimitService;
    }

    @GetMapping("/{publicCode}/quota")
    @ApiResponse(responseCode = "200", description = "Upload quota of the current device")
    @ApiResponse(responseCode = "404", description = "Event does not exist or is not active", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ProblemDetail.class)))
    QuotaResponse getQuota(@PathVariable String publicCode, @RequestAttribute(GuestIdFilter.ATTRIBUTE) UUID guestId) {
        return guestLimitService.getQuota(publicCode, guestId);
    }
}
