package com.picturepov.api.event;

import com.picturepov.api.invitation.InvitationRole;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

public record MyEventResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String publicCode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate eventDate,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EventStatus status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "The signed-in user's role on the event") InvitationRole role
) {}
