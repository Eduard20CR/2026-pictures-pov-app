package com.picturepov.api.event;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateEventRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "Boda Ana y Luis")
        @NotBlank @Size(max = 100) String name,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "2026-12-20")
        @NotNull LocalDate eventDate,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "ana@example.com")
        @NotBlank @Email @Size(max = 320) String ownerEmail
) {
}