package com.picturepov.api.zipexport;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** The event's latest ZIP (RF-ORG-14). */
record ZipExportResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "IN_PROGRESS while queued or running, AVAILABLE once it can be downloaded, FAILED otherwise") State status,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant requestedAt,
        @Schema(description = "Photos in the ZIP; only when AVAILABLE") Integer imageCount,
        @Schema(description = "Size of the ZIP in bytes; only when AVAILABLE") Long sizeBytes,
        @Schema(description = "Temporary signed link that downloads the ZIP, valid for P-07 (RNF-SEG-05); only when AVAILABLE") String downloadUrl
) {
    enum State { IN_PROGRESS, AVAILABLE, FAILED }
}
