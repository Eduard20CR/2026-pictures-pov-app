package com.picturepov.api.image;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

public record UploadResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID imageId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String uploadUrl,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant expiresAt
) {
}