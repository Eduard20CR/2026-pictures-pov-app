package com.picturepov.api.invitation;

import com.picturepov.api.common.error.ApiProblem;
import com.picturepov.api.common.error.BadRequestException;
import com.picturepov.api.common.error.ErrorCode;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** Invitations addressed to the signed-in user's email (RF-AUT-05). */
@RestController
class MyInvitationsController {

    private final MyInvitationsService myInvitationsService;

    MyInvitationsController(MyInvitationsService myInvitationsService) {
        this.myInvitationsService = myInvitationsService;
    }

    @GetMapping("/me/invitations")
    @ApiResponse(responseCode = "200", description = "Pending invitations for the signed-in user's verified email, newest first; empty if the email is not verified")
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    List<PendingInvitationResponse> listMyPendingInvitations(@AuthenticationPrincipal Jwt jwt) {
        return myInvitationsService.listPending(jwt);
    }

    @PatchMapping("/me/invitations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @ApiResponse(responseCode = "204", description = "Accepted and linked to the signed-in user: the event joins their events with the invitation's role")
    @ApiResponse(responseCode = "400", description = "Status missing or other than ACCEPTED",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "401", description = "Missing or invalid access token",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "403", description = "Email not verified, or the invitation is for another email",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    @ApiResponse(responseCode = "404", description = "No Pending invitation with that id: removed, already accepted, its event deleted, or never existed",
            content = @Content(mediaType = "application/problem+json", schema = @Schema(implementation = ApiProblem.class)))
    void updateInvitation(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt,
                          @Valid @RequestBody UpdateInvitationRequest request) {
        if (request.status() != InvitationStatus.ACCEPTED) {
            throw new BadRequestException(ErrorCode.VALIDATION_FAILED, "Only ACCEPTED is allowed");
        }
        myInvitationsService.accept(id, jwt);
    }
}
