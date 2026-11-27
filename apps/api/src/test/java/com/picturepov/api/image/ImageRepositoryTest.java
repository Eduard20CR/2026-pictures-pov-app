package com.picturepov.api.image;

import com.picturepov.api.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Limit;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/** The queries behind the review pages and downloads (ADR 0016). */
@SpringBootTest
@Transactional
class ImageRepositoryTest {

    @Autowired ImageRepository sut;
    @Autowired JdbcClient jdbc;

    TestData data;
    UUID event;

    @BeforeEach
    void setUp() {
        data = new TestData(jdbc);
        event = data.event();
    }

    @Test
    void processedPhotoOfTheEventIsFound() {
        // Arrange
        UUID photo = data.photo(event, "2099-01-01T10:00:00Z", "READY");

        // Act
        Optional<Image> found = sut.findByIdAndEventIdAndStatus(photo, event, ImageStatus.READY);

        // Assert
        assertThat(found).map(Image::getId).contains(photo);
    }

    @Test
    void photoOfAnotherEventOrStillProcessingIsNotFound() {
        // Arrange
        UUID otherEventPhoto = data.photo(data.event(), "2099-01-01T10:00:00Z", "READY");
        UUID processing = data.photo(event, "2099-01-01T10:00:00Z", "PENDING");

        // Act
        Optional<Image> fromOtherEvent = sut.findByIdAndEventIdAndStatus(otherEventPhoto, event, ImageStatus.READY);
        Optional<Image> stillProcessing = sut.findByIdAndEventIdAndStatus(processing, event, ImageStatus.READY);

        // Assert
        assertThat(fromOtherEvent).isEmpty();
        assertThat(stillProcessing).isEmpty();
    }

    @Test
    void firstPageIsEveryPhotoOfTheEventNewestFirstWithTiesBrokenById() {
        // Arrange
        UUID oldest = data.photo(event, "2099-01-01T09:00:00Z", "READY");
        List<UUID> ties = newestFirst(
                data.photo(event, "2099-01-01T10:00:00Z", "READY"),
                data.photo(event, "2099-01-01T10:00:00Z", "PENDING"));
        data.photo(data.event(), "2099-01-01T11:00:00Z", "READY");

        // Act
        List<Image> page = sut.findByEventIdOrderByCreatedAtDescIdDesc(event, Limit.of(10));

        // Assert
        assertThat(page).extracting(Image::getId).containsExactly(ties.get(0), ties.get(1), oldest);
    }

    @Test
    void pageAfterACursorContinuesWithoutRepeatingOrSkippingTies() {
        // Arrange: the cursor points at the first of two photos uploaded at the same instant
        UUID oldest = data.photo(event, "2099-01-01T09:00:00Z", "READY");
        List<UUID> ties = newestFirst(
                data.photo(event, "2099-01-01T10:00:00Z", "READY"),
                data.photo(event, "2099-01-01T10:00:00Z", "READY"));

        // Act
        List<Image> page = sut.findPageAfter(event, Instant.parse("2099-01-01T10:00:00Z"), ties.get(0), Limit.of(10));

        // Assert
        assertThat(page).extracting(Image::getId).containsExactly(ties.get(1), oldest);
    }

    @Test
    void pageIsCutAtTheLimit() {
        // Arrange
        UUID newest = data.photo(event, "2099-01-01T11:00:00Z", "READY");
        data.photo(event, "2099-01-01T10:00:00Z", "READY");

        // Act
        List<Image> page = sut.findByEventIdOrderByCreatedAtDescIdDesc(event, Limit.of(1));

        // Assert
        assertThat(page).extracting(Image::getId).containsExactly(newest);
    }

    /** Same instant: the larger id comes first, as Postgres orders uuids. */
    private static List<UUID> newestFirst(UUID... sameInstant) {
        return Stream.of(sameInstant).sorted(Comparator.comparing(UUID::toString).reversed()).toList();
    }
}
