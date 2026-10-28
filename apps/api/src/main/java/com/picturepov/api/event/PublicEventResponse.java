package com.picturepov.api.event;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record PublicEventResponse(
        @Schema(requiredMode = REQUIRED) String publicCode,
        @Schema(requiredMode = REQUIRED) String name,
        @Schema(requiredMode = REQUIRED) String color,
        @Schema(requiredMode = REQUIRED) LocalDate eventDate,
        @Schema(requiredMode = REQUIRED) int imagesPerUser
) {
    static PublicEventResponse from(Event event) {
        return new PublicEventResponse(
                event.getPublicCode(),
                event.getName(),
                event.getColor(),
                event.getEventDate(),
                event.getImagesPerUser()
        );
    }
}