package com.picturepov.api.event;

import com.picturepov.api.invitation.Invitation;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;
import java.util.UUID;

public record AdminEventResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String publicCode,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) LocalDate eventDate,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) EventStatus status,
        @Schema(description = "Null when the event has no owner invitation") String ownerEmail
) {
    static AdminEventResponse from(Event event, Invitation owner) {
        return new AdminEventResponse(
                event.getId(), event.getPublicCode(), event.getName(),
                event.getEventDate(), event.getStatus(), owner.getEmail());
    }
}