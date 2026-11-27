package com.picturepov.api.zipexport;

import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.event.EventFixtures;
import com.picturepov.api.image.ImageFixtures;
import com.picturepov.api.invitation.InvitationRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static com.picturepov.api.zipexport.ZipExportFixtures.readyZipExport;
import static com.picturepov.api.zipexport.ZipExportFixtures.zipExport;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ZipExportServiceTest {

    static final Duration MAX_RUN = Duration.ofMinutes(48);
    static final Duration DOWNLOAD_URL_TTL = Duration.ofHours(24);

    @Mock ZipExportRepository zipExportRepository;
    @Mock ZipExportQueue queue;

    UUID eventId = UUID.randomUUID();
    UUID userId = UUID.randomUUID();

    @Test
    void organizerRequestsTheZipAndItIsQueuedInProgress() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.empty());
        when(zipExportRepository.hasProcessedPhotos(eventId)).thenReturn(true);
        when(zipExportRepository.saveAndFlush(any(ZipExport.class))).thenAnswer(call -> call.getArgument(0));
        ArgumentCaptor<ZipExport> stored = ArgumentCaptor.forClass(ZipExport.class);

        // Act
        ZipExportResponse response = sut.request(eventId, userId);

        // Assert
        assertThat(response.status()).isEqualTo(ZipExportResponse.State.IN_PROGRESS);
        assertThat(response.downloadUrl()).isNull();
        verify(zipExportRepository).saveAndFlush(stored.capture());
        assertThat(stored.getValue().getStatus()).isEqualTo(ZipExportStatus.PENDING);
        verify(queue).send(stored.getValue().getId());
    }

    @Test
    void requestingWhileAZipIsInProgressStartsNoSecondGeneration() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        ZipExport running = zipExport(ZipExportStatus.RUNNING, Instant.now().minus(Duration.ofMinutes(10)));
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.of(running));

        // Act
        ZipExportResponse response = sut.request(eventId, userId);

        // Assert
        assertThat(response.status()).isEqualTo(ZipExportResponse.State.IN_PROGRESS);
        assertThat(response.requestedAt()).isEqualTo(running.getCreatedAt());
        verify(zipExportRepository, never()).saveAndFlush(any(ZipExport.class));
        verifyNoInteractions(queue);
    }

    @ParameterizedTest
    @EnumSource(value = ZipExportStatus.class, names = {"READY", "FAILED"})
    void finishedZipCanBeRequestedAgain(ZipExportStatus status) {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        ZipExport previous = zipExport(status, Instant.now().minus(Duration.ofMinutes(5)));
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.of(previous));
        when(zipExportRepository.hasProcessedPhotos(eventId)).thenReturn(true);
        when(zipExportRepository.saveAndFlush(any(ZipExport.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        ZipExportResponse response = sut.request(eventId, userId);

        // Assert
        assertThat(response.status()).isEqualTo(ZipExportResponse.State.IN_PROGRESS);
        verify(queue).send(any(UUID.class));
        verify(queue, never()).send(previous.getId());
    }

    @Test
    void zipStillRunningAfterMaxRunIsReplacedByANewOne() {
        // Arrange: the worker timed out on its last receive, so nothing marked it failed
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        ZipExport stuck = zipExport(ZipExportStatus.RUNNING, Instant.now().minus(Duration.ofMinutes(49)));
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.of(stuck));
        when(zipExportRepository.hasProcessedPhotos(eventId)).thenReturn(true);
        when(zipExportRepository.saveAndFlush(any(ZipExport.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        ZipExportResponse response = sut.request(eventId, userId);

        // Assert
        assertThat(response.status()).isEqualTo(ZipExportResponse.State.IN_PROGRESS);
        assertThat(response.requestedAt()).isAfter(stuck.getCreatedAt());
        verify(queue, never()).send(stuck.getId());
    }

    @Test
    void eventWithoutProcessedPhotosGets409AndNothingIsQueued() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.empty());
        when(zipExportRepository.hasProcessedPhotos(eventId)).thenReturn(false);

        // Act
        Throwable thrown = catchThrowable(() -> sut.request(eventId, userId));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.NO_PHOTOS_TO_EXPORT);
        verify(zipExportRepository, never()).saveAndFlush(any(ZipExport.class));
        verifyNoInteractions(queue);
    }

    @Test
    void zipThatCouldNotBeQueuedIsMarkedFailedSoItCanBeRequestedAgain() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.empty());
        when(zipExportRepository.hasProcessedPhotos(eventId)).thenReturn(true);
        when(zipExportRepository.saveAndFlush(any(ZipExport.class))).thenAnswer(call -> call.getArgument(0));
        IllegalStateException sqsDown = new IllegalStateException("SQS unavailable");
        doThrow(sqsDown).when(queue).send(any(UUID.class));
        ArgumentCaptor<ZipExport> stored = ArgumentCaptor.forClass(ZipExport.class);

        // Act
        Throwable thrown = catchThrowable(() -> sut.request(eventId, userId));

        // Assert
        assertThat(thrown).isSameAs(sqsDown);
        verify(zipExportRepository).saveAndFlush(stored.capture());
        verify(zipExportRepository).markNotQueued(stored.getValue().getId(), "Not queued: SQS unavailable");
    }

    @Test
    void longQueueErrorIsCutToFitTheErrorColumn() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.empty());
        when(zipExportRepository.hasProcessedPhotos(eventId)).thenReturn(true);
        when(zipExportRepository.saveAndFlush(any(ZipExport.class))).thenAnswer(call -> call.getArgument(0));
        doThrow(new IllegalStateException("x".repeat(2000))).when(queue).send(any(UUID.class));
        ArgumentCaptor<String> reason = ArgumentCaptor.forClass(String.class);

        // Act
        catchThrowable(() -> sut.request(eventId, userId));

        // Assert
        verify(zipExportRepository).markNotQueued(any(UUID.class), reason.capture());
        assertThat(reason.getValue()).hasSize(1000).startsWith("Not queued: x");
    }

    @Test
    void moderatorCannotRequestNorFollowTheZip() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.MODERATOR);

        // Act
        Throwable onRequest = catchThrowable(() -> sut.request(eventId, userId));
        Throwable onLatest = catchThrowable(() -> sut.latest(eventId, userId));

        // Assert
        assertThat(onRequest).isInstanceOf(AccessDeniedException.class);
        assertThat(onLatest).isInstanceOf(AccessDeniedException.class);
        verifyNoInteractions(zipExportRepository, queue);
    }

    @Test
    void userWithoutAnAcceptedInvitationGets404() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        UUID stranger = UUID.randomUUID();

        // Act
        Throwable onRequest = catchThrowable(() -> sut.request(eventId, stranger));
        Throwable onLatest = catchThrowable(() -> sut.latest(eventId, stranger));

        // Assert
        assertThat(onRequest).isInstanceOf(NotFoundException.class).extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
        assertThat(onLatest).isInstanceOf(NotFoundException.class).extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
        verifyNoInteractions(zipExportRepository, queue);
    }

    @Test
    void eventWithoutAZipHasNoLatest() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.empty());

        // Act
        Optional<ZipExportResponse> latest = sut.latest(eventId, userId);

        // Assert
        assertThat(latest).isEmpty();
    }

    @Test
    void availableZipComesWithASignedDownloadLinkValidForP07() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        ZipExport ready = readyZipExport();
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.of(ready));

        // Act
        ZipExportResponse response = sut.latest(eventId, userId).orElseThrow();

        // Assert
        assertThat(response.status()).isEqualTo(ZipExportResponse.State.AVAILABLE);
        assertThat(response.imageCount()).isEqualTo(12);
        assertThat(response.sizeBytes()).isEqualTo(3_400_000L);
        assertThat(response.downloadUrl())
                .contains("zips/" + ready.getId() + ".zip")
                .contains("X-Amz-Signature=")
                .contains("X-Amz-Expires=86400")
                .contains("response-content-disposition=attachment");
    }

    @Test
    void zipStillRunningIsShownInProgress() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        ZipExport running = zipExport(ZipExportStatus.RUNNING, Instant.now().minus(Duration.ofMinutes(47)));
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.of(running));

        // Act
        ZipExportResponse response = sut.latest(eventId, userId).orElseThrow();

        // Assert
        assertThat(response.status()).isEqualTo(ZipExportResponse.State.IN_PROGRESS);
    }

    @Test
    void failedOrStuckZipIsShownFailedWithoutALink() {
        // Arrange
        ZipExportService sut = serviceFor(InvitationRole.OWNER);
        ZipExport stuck = zipExport(ZipExportStatus.RUNNING, Instant.now().minus(Duration.ofMinutes(49)));
        when(zipExportRepository.findFirstByEventIdOrderByCreatedAtDesc(eventId)).thenReturn(Optional.of(stuck));

        // Act
        ZipExportResponse response = sut.latest(eventId, userId).orElseThrow();

        // Assert
        assertThat(response.status()).isEqualTo(ZipExportResponse.State.FAILED);
        assertThat(response.downloadUrl()).isNull();
        assertThat(response.imageCount()).isNull();
    }

    /** userId holds the role on eventId; anyone else has no access. The event row lock is a database concern, so it is stubbed. */
    private ZipExportService serviceFor(InvitationRole role) {
        return new ZipExportService(EventFixtures.eventAccess(eventId, userId, role), zipExportRepository, queue,
                mock(JdbcClient.class, RETURNS_DEEP_STUBS), new NoDatabaseTransactions(), MAX_RUN,
                ImageFixtures.photoLinkSigner(), DOWNLOAD_URL_TTL);
    }

    /** Runs the service's transaction callbacks with no database behind them. */
    private static class NoDatabaseTransactions extends AbstractPlatformTransactionManager {
        @Override protected Object doGetTransaction() { return new Object(); }
        @Override protected void doBegin(Object transaction, TransactionDefinition definition) {}
        @Override protected void doCommit(DefaultTransactionStatus status) {}
        @Override protected void doRollback(DefaultTransactionStatus status) {}
    }
}
