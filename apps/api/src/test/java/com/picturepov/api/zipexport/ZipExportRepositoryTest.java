package com.picturepov.api.zipexport;

import com.picturepov.api.TestData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** The queries behind the ZIP: which photos count, which ZIP is the latest, and marking one never queued. */
@SpringBootTest
@Transactional
class ZipExportRepositoryTest {

    @Autowired ZipExportRepository sut;
    @Autowired JdbcClient jdbc;

    TestData data;
    UUID event;
    UUID organizer;

    @BeforeEach
    void setUp() {
        data = new TestData(jdbc);
        event = data.event();
        organizer = data.user("ana@example.com");
    }

    @Test
    void eventWithAProcessedPhotoHasPhotosToExport() {
        // Arrange
        data.photo(event, "2099-01-01T10:00:00Z", "READY");

        // Act
        boolean hasPhotos = sut.hasProcessedPhotos(event);

        // Assert
        assertThat(hasPhotos).isTrue();
    }

    @Test
    void photosStillProcessingOrOfAnotherEventDoNotCount() {
        // Arrange
        data.photo(event, "2099-01-01T10:00:00Z", "PENDING");
        data.photo(data.event(), "2099-01-01T10:00:00Z", "READY");

        // Act
        boolean hasPhotos = sut.hasProcessedPhotos(event);

        // Assert
        assertThat(hasPhotos).isFalse();
    }

    @Test
    void latestZipIsTheMostRecentlyRequestedOfTheEvent() {
        // Arrange
        data.zipExport(event, organizer, "FAILED", "2099-01-01T09:00:00Z");
        UUID latest = data.zipExport(event, organizer, "RUNNING", "2099-01-01T10:00:00Z");
        data.zipExport(data.event(), organizer, "PENDING", "2099-01-01T11:00:00Z");

        // Act
        Optional<ZipExport> found = sut.findFirstByEventIdOrderByCreatedAtDesc(event);

        // Assert
        assertThat(found).map(ZipExport::getId).contains(latest);
    }

    @Test
    void zipNeverQueuedIsFailedWithTheReason() {
        // Arrange
        UUID export = data.zipExport(event, organizer, "PENDING", "2099-01-01T10:00:00Z");

        // Act
        sut.markNotQueued(export, "Not queued: SQS unavailable");

        // Assert
        ZipExport stored = sut.findById(export).orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(ZipExportStatus.FAILED);
        assertThat(stored.getCompletedAt()).isNotNull();
        assertThat(stored).extracting("errorMessage").isEqualTo("Not queued: SQS unavailable");
    }

    @Test
    void zipTheWorkerAlreadyTookIsNotMarkedFailed() {
        // Arrange
        UUID export = data.zipExport(event, organizer, "RUNNING", "2099-01-01T10:00:00Z");

        // Act
        sut.markNotQueued(export, "Not queued: SQS unavailable");

        // Assert
        assertThat(sut.findById(export).orElseThrow().getStatus()).isEqualTo(ZipExportStatus.RUNNING);
    }
}
