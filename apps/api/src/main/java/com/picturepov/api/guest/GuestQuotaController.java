package com.picturepov.api.guest;

import com.picturepov.api.common.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/events")
class GuestQuotaController {
    private final GuestQuotaService guestQuotaService;

    GuestQuotaController(GuestQuotaService guestQuotaService) {
        this.guestQuotaService = guestQuotaService;
    }

    @GetMapping("/{publicCode}/quota")
    @ApiResponse(responseCode = "200", description = "Upload quota of the current device")
    @ApiResponse(responseCode = "404", description = "Event does not exist, is archived or was deleted", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "409", description = "Event is closed and no longer accepts photos", content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    UploadQuotaResponse getUploadQuota(@PathVariable String publicCode, @RequestAttribute(GuestIdFilter.ATTRIBUTE) UUID guestId) {
        return guestQuotaService.getUploadQuota(publicCode, guestId);
    }
}
