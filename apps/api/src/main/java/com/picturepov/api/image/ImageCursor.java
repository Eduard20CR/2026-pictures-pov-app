package com.picturepov.api.image;

import com.picturepov.api.common.error.BadRequestException;
import com.picturepov.api.common.error.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

/** Position after the last photo of a page: newest first, ties broken by id (ADR 0016). Opaque to the client. */
record ImageCursor(Instant createdAt, UUID id) {

    static ImageCursor after(Image image) {
        return new ImageCursor(image.getCreatedAt(), image.getId());
    }

    String encode() {
        String raw = createdAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    static ImageCursor decode(String cursor) {
        try {
            String[] parts = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8).split("\\|", 2);
            return new ImageCursor(Instant.parse(parts[0]), UUID.fromString(parts[1]));
        } catch (RuntimeException e) {
            throw new BadRequestException(ErrorCode.INVALID_CURSOR, "Malformed cursor");
        }
    }
}
