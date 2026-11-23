package com.picturepov.api.invitation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

public record PendingInvitationResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID eventId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String eventName,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) InvitationRole role
) {}
