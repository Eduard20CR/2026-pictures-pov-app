package com.picturepov.api.image;

import io.swagger.v3.oas.annotations.media.Schema;

public record UploadRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "image/jpeg") String contentType,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, example = "3145728") long sizeBytes
) {
}