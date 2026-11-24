package com.picturepov.api.image;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

/** Cursor page (ADR 0016). */
public record ReviewImagePage(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<ReviewImageResponse> items,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, types = {"string", "null"},
                description = "Pass as cursor for the next page; null on the last page") String nextCursor
) {
}
