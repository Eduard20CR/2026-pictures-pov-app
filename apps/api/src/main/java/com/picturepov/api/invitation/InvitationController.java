package com.picturepov.api.invitation;

import com.picturepov.api.common.error.ApiProblem;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
class InvitationController {

    /** The signed-in user's id: the token's sub claim. */
    private static final String USER_ID = "T(java.util.UUID).fromString(subject)";

    private final InvitationService invitationService;

    InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    @PostMapping("/me/events/{id}/invitations")
    @ResponseStatus(HttpStatus.CREATED)
    @ApiResponse(responseCode = "201", description = "Pending MODERATOR invitation for the email, even if it has no account")
    @ApiResponse(responseCode = "400", description = "Invalid or missing email",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "403", description = "Moderator of the event: inviting is organizer-only",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "404", description = "No Accepted invitation to the event, or it does not exist or was deleted",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "409", description = "The email already has an invitation to the event",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    InvitationResponse inviteModerator(@PathVariable UUID id,
                                       @AuthenticationPrincipal(expression = USER_ID) UUID userId,
                                       @Valid @RequestBody InviteModeratorRequest request) {
        return invitationService.inviteModerator(id, userId, request.email());
    }
}
