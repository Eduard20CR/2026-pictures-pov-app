package com.picturepov.api.guest;

import io.swagger.v3.oas.annotations.media.Schema;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

public record QuotaResponse(
        @Schema(requiredMode = REQUIRED) int limit,
        @Schema(requiredMode = REQUIRED) int used,
        @Schema(requiredMode = REQUIRED) int remaining
) {
    static QuotaResponse of(int limit, int used) {
        return new QuotaResponse(limit, used, Math.max(0, limit - used));
    }
}