package com.picturepov.api.invitation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record InvitationResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Stored in lowercase") String email,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) InvitationRole role,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) InvitationStatus status
) {
    static InvitationResponse from(Invitation invitation) {
        return new InvitationResponse(invitation.getId(), invitation.getEmail(), invitation.getRole(), invitation.getStatus());
    }
}
