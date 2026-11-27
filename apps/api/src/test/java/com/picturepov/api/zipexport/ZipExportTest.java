package com.picturepov.api.zipexport;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.Duration;
import java.time.Instant;

import static com.picturepov.api.zipexport.ZipExportFixtures.zipExport;
import static org.assertj.core.api.Assertions.assertThat;

class ZipExportTest {

    static final Duration MAX_RUN = Duration.ofMinutes(48);
    static final Instant NOW = Instant.parse("2099-01-01T12:00:00Z");

    @ParameterizedTest
    @EnumSource(value = ZipExportStatus.class, names = {"PENDING", "RUNNING"})
    void queuedOrRunningZipIsInProgressWithinMaxRun(ZipExportStatus status) {
        // Arrange
        ZipExport sut = zipExport(status, NOW.minus(Duration.ofMinutes(47)));

        // Act
        boolean inProgress = sut.isInProgress(NOW, MAX_RUN);

        // Assert
        assertThat(inProgress).isTrue();
    }

    @ParameterizedTest
    @EnumSource(value = ZipExportStatus.class, names = {"PENDING", "RUNNING"})
    void zipStillQueuedOrRunningAtMaxRunCountsAsFailed(ZipExportStatus status) {
        // Arrange: the worker timed out or crashed on its last receive, so nothing marked it failed
        ZipExport sut = zipExport(status, NOW.minus(MAX_RUN));

        // Act
        boolean inProgress = sut.isInProgress(NOW, MAX_RUN);

        // Assert
        assertThat(inProgress).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = ZipExportStatus.class, names = {"READY", "FAILED"})
    void finishedZipIsNotInProgress(ZipExportStatus status) {
        // Arrange
        ZipExport sut = zipExport(status, NOW);

        // Act
        boolean inProgress = sut.isInProgress(NOW, MAX_RUN);

        // Assert
        assertThat(inProgress).isFalse();
    }

    @ParameterizedTest
    @EnumSource(ZipExportStatus.class)
    void onlyAReadyZipIsAvailable(ZipExportStatus status) {
        // Arrange
        ZipExport sut = zipExport(status, NOW);

        // Act
        boolean available = sut.isAvailable();

        // Assert
        assertThat(available).isEqualTo(status == ZipExportStatus.READY);
    }
}
