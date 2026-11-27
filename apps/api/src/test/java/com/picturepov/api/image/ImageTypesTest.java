package com.picturepov.api.image;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ImageTypesTest {

    @ParameterizedTest
    @CsvSource({"image/jpeg, .jpg", "image/png, .png", "image/webp, .webp", "image/heic, .heic"})
    void allowedTypeDownloadsWithItsExtension(String contentType, String extension) {
        // Act
        boolean allowed = ImageTypes.isAllowed(contentType);
        String actual = ImageTypes.extension(contentType);

        // Assert
        assertThat(allowed).isTrue();
        assertThat(actual).isEqualTo(extension);
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"image/gif", "IMAGE/JPEG", "application/pdf", ""})
    void otherTypesAreNotAllowed(String contentType) {
        // Act
        boolean allowed = ImageTypes.isAllowed(contentType);

        // Assert
        assertThat(allowed).isFalse();
    }

    @Test
    void unknownTypeHasNoExtension() {
        // Act
        String extension = ImageTypes.extension("image/gif");

        // Assert
        assertThat(extension).isEmpty();
    }
}
