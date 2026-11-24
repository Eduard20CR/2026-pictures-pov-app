package com.picturepov.api.image;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;
import java.util.UUID;

/** A photo in the review. */
public record ReviewImageResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) UUID id,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, types = {"string", "null"},
                description = "Temporary signed link (RNF-SEG-05); null until the thumbnail is generated") String thumbnailUrl,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "Left out of the gallery") boolean hidden,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt
) {
}
