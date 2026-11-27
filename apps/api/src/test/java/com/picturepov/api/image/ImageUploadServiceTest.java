package com.picturepov.api.image;

import com.picturepov.api.common.error.BadRequestException;
import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.event.Event;
import com.picturepov.api.event.EventFixtures;
import com.picturepov.api.event.EventStatus;
import com.picturepov.api.event.GuestEventService;
import com.picturepov.api.guest.InMemoryGuestLimits;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static com.picturepov.api.event.EventFixtures.PUBLIC_CODE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    static final long MAX_SIZE_BYTES = 20_971_520L;
    static final Duration UPLOAD_URL_TTL = Duration.ofMinutes(5);

    @Mock ImageRepository imageRepository;

    UUID guestId = UUID.randomUUID();

    @Test
    void guestGetsASignedUrlToUploadThePhoto() {
        // Arrange
        Event event = EventFixtures.openEvent(10);
        ImageUploadService sut = serviceFor(event);
        when(imageRepository.save(any(Image.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        PresignedUploadResponse response = sut.createPresignedUpload(PUBLIC_CODE, guestId, jpeg(1000));

        // Assert
        assertThat(response.uploadUrl())
                .contains(ImageFixtures.BUCKET)
                .contains("originals/" + event.getId() + "/" + response.imageId())
                .contains("X-Amz-Expires=300");
        assertThat(response.expiresAt()).isCloseTo(Instant.now().plus(UPLOAD_URL_TTL), within(Duration.ofSeconds(5)));
    }

    @Test
    void thePhotoIsStoredPendingUntilItsThumbnailExists() {
        // Arrange
        Event event = EventFixtures.openEvent(10);
        ImageUploadService sut = serviceFor(event);
        when(imageRepository.save(any(Image.class))).thenAnswer(call -> call.getArgument(0));
        ArgumentCaptor<Image> stored = ArgumentCaptor.forClass(Image.class);

        // Act
        PresignedUploadResponse response = sut.createPresignedUpload(PUBLIC_CODE, guestId, jpeg(1000));

        // Assert
        verify(imageRepository).save(stored.capture());
        assertThat(stored.getValue().getId()).isEqualTo(response.imageId());
        assertThat(stored.getValue()).extracting("status", "guestId", "contentType", "sizeBytes")
                .containsExactly(ImageStatus.PENDING, guestId, "image/jpeg", 1000L);
    }

    @Test
    void signedUrlOnlyCreatesThePhotoSoItCannotReplaceIt() {
        // Arrange
        ImageUploadService sut = serviceFor(EventFixtures.openEvent(10));
        when(imageRepository.save(any(Image.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        PresignedUploadResponse response = sut.createPresignedUpload(PUBLIC_CODE, guestId, jpeg(1000));

        // Assert: S3 refuses the PUT with 412 once the object exists (conditional write)
        assertThat(response.uploadUrl()).containsPattern("X-Amz-SignedHeaders=[^&]*if-none-match");
    }

    @ParameterizedTest
    @CsvSource({
            "image/gif,  1000,     UNSUPPORTED_CONTENT_TYPE",
            "image/jpeg, 0,        INVALID_FILE_SIZE",
            "image/jpeg, 20971521, FILE_TOO_LARGE"
    })
    void invalidFileGets400AndUsesNoUpload(String contentType, long sizeBytes, ErrorCode code) {
        // Arrange
        ImageUploadService sut = serviceFor(EventFixtures.openEvent(10));

        // Act
        Throwable thrown = catchThrowable(() ->
                sut.createPresignedUpload(PUBLIC_CODE, guestId, new PresignedUploadRequest(contentType, sizeBytes)));

        // Assert
        assertThat(thrown).isInstanceOf(BadRequestException.class).extracting("code").isEqualTo(code);
        verifyNoInteractions(imageRepository);
    }

    @Test
    void largestAllowedFileIsAccepted() {
        // Arrange
        ImageUploadService sut = serviceFor(EventFixtures.openEvent(10));
        when(imageRepository.save(any(Image.class))).thenAnswer(call -> call.getArgument(0));

        // Act
        Throwable thrown = catchThrowable(() -> sut.createPresignedUpload(PUBLIC_CODE, guestId, jpeg(MAX_SIZE_BYTES)));

        // Assert
        assertThat(thrown).isNull();
    }

    @Test
    void guestAtTheUploadLimitGets409AndNoPhotoIsStored() {
        // Arrange
        ImageUploadService sut = serviceFor(EventFixtures.openEvent(1));
        when(imageRepository.save(any(Image.class))).thenAnswer(call -> call.getArgument(0));
        sut.createPresignedUpload(PUBLIC_CODE, guestId, jpeg(1000));

        // Act
        Throwable thrown = catchThrowable(() -> sut.createPresignedUpload(PUBLIC_CODE, guestId, jpeg(1000)));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.UPLOAD_LIMIT_REACHED);
        verify(imageRepository, times(1)).save(any(Image.class));
    }

    @Test
    void closedEventGets409AndNoPhotoIsStored() {
        // Arrange
        ImageUploadService sut = serviceFor(EventFixtures.event(EventStatus.CLOSED, 10));

        // Act
        Throwable thrown = catchThrowable(() -> sut.createPresignedUpload(PUBLIC_CODE, guestId, jpeg(1000)));

        // Assert
        assertThat(thrown).isInstanceOf(ConflictException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_CLOSED);
        verifyNoInteractions(imageRepository);
    }

    @Test
    void unknownEventGets404() {
        // Arrange
        ImageUploadService sut = serviceFor(EventFixtures.openEvent(10));

        // Act
        Throwable thrown = catchThrowable(() -> sut.createPresignedUpload("unknownCode", guestId, jpeg(1000)));

        // Assert
        assertThat(thrown).isInstanceOf(NotFoundException.class)
                .extracting("code").isEqualTo(ErrorCode.EVENT_NOT_FOUND);
        verifyNoInteractions(imageRepository);
    }

    private ImageUploadService serviceFor(Event event) {
        GuestEventService guestEventService = EventFixtures.guestEventService(event);
        return new ImageUploadService(guestEventService, InMemoryGuestLimits.guestQuotaService(guestEventService),
                imageRepository, ImageFixtures.presigner(), ImageFixtures.BUCKET, MAX_SIZE_BYTES, UPLOAD_URL_TTL);
    }

    private static PresignedUploadRequest jpeg(long sizeBytes) {
        return new PresignedUploadRequest("image/jpeg", sizeBytes);
    }
}
