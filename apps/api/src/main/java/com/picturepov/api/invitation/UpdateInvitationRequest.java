package com.picturepov.api.invitation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateInvitationRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = "ACCEPTED", description = "Only ACCEPTED: declining is out of scope")
        @NotNull InvitationStatus status
) {}
