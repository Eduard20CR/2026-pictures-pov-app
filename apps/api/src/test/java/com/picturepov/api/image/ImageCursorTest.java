package com.picturepov.api.image;

import com.picturepov.api.common.error.BadRequestException;
import com.picturepov.api.common.error.ErrorCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

class ImageCursorTest {

    @Test
    void decodingAnEncodedCursorGivesTheSamePosition() {
        // Arrange
        ImageCursor cursor = new ImageCursor(Instant.parse("2099-01-01T10:00:00.123456Z"), UUID.randomUUID());

        // Act
        ImageCursor decoded = ImageCursor.decode(cursor.encode());

        // Assert
        assertThat(decoded).isEqualTo(cursor);
    }

    @Test
    void encodedCursorIsUrlSafe() {
        // Arrange
        ImageCursor cursor = new ImageCursor(Instant.parse("2099-01-01T10:00:00Z"), UUID.randomUUID());

        // Act
        String encoded = cursor.encode();

        // Assert
        assertThat(encoded).matches("[A-Za-z0-9_-]+");
    }

    @ParameterizedTest
    @ValueSource(strings = {"not a cursor!", "bm8tc2VwYXJhdG9y", "MjA5OS0wMS0wMXxub3QtYS11dWlk", ""})
    void malformedCursorGets400(String cursor) {
        // Act
        Throwable thrown = catchThrowable(() -> ImageCursor.decode(cursor));

        // Assert
        assertThat(thrown).isInstanceOf(BadRequestException.class)
                .extracting("code").isEqualTo(ErrorCode.INVALID_CURSOR);
    }

    @Test
    void cursorWithAValidInstantButNoIdGets400() {
        // Arrange
        String cursor = Base64.getUrlEncoder().encodeToString("2099-01-01T10:00:00Z".getBytes(StandardCharsets.UTF_8));

        // Act
        Throwable thrown = catchThrowable(() -> ImageCursor.decode(cursor));

        // Assert
        assertThat(thrown).isInstanceOf(BadRequestException.class);
    }
}
