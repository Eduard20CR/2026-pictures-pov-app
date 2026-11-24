package com.picturepov.api.image;

import io.swagger.v3.oas.annotations.media.Schema;

/** Where to download a photo's original. */
public record ImageDownloadResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED,
                description = "Temporary signed link to the original file, served as a download (RNF-SEG-05)") String url
) {
}
