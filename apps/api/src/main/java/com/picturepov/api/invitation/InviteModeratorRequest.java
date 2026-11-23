package com.picturepov.api.invitation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InviteModeratorRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "beto@example.com")
        @NotBlank @Email @Size(max = 320) String email
) {
}
