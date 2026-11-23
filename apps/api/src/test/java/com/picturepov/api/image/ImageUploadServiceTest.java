package com.picturepov.api.image;

import com.picturepov.api.common.error.BadRequestException;
import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.event.Event;
import com.picturepov.api.event.GuestEventService;
import com.picturepov.api.guest.GuestQuotaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @Mock GuestEventService guestEventService;
    @Mock GuestQuotaService guestQuotaService;
    @Mock ImageRepository imageRepository;

    // A real presigner: signing a URL is local math, it never calls AWS.
    S3Presigner s3Presigner = S3Presigner.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
            .build();

    ImageUploadService imageUploadService;

    UUID guestId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        imageUploadService = new ImageUploadService(
                guestEventService, guestQuotaService, imageRepository, s3Presigner,
                "test-bucket", 20_971_520L, Duration.ofMinutes(5));
    }

    @Test
    void rejectsUnsupportedContentType(){
        assertThatThrownBy(()->imageUploadService.createPresignedUpload("abc", UUID.randomUUID(), new PresignedUploadRequest("image/gif", 1000)))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(guestQuotaService);
    }

    @Test
    void rejectsFileLargerThanLimit(){
        assertThatThrownBy(()->imageUploadService.createPresignedUpload("abc", UUID.randomUUID(), new PresignedUploadRequest("image/jpeg", 20_971_521L)))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(guestQuotaService);
    }

    @Test
    void rejectsEmptyFileAsInvalidSize(){
        assertThatThrownBy(()->imageUploadService.createPresignedUpload("abc", UUID.randomUUID(), new PresignedUploadRequest("image/jpeg", 0)))
                .isInstanceOfSatisfying(BadRequestException.class, ex -> assertThat(ex.getCode()).isEqualTo(ErrorCode.INVALID_FILE_SIZE));
        verifyNoInteractions(guestQuotaService);
    }

    @Test
    void reservesQuotaThenReturnsASignedUrlForThePhoto() {
        UUID eventId = UUID.randomUUID();
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(event.getImagesPerUser()).thenReturn(10);
        when(guestEventService.getOpenEvent("abc")).thenReturn(event);
        when(imageRepository.save(any(Image.class))).thenAnswer(call -> call.getArgument(0));

        PresignedUploadResponse response = imageUploadService.createPresignedUpload("abc", guestId, new PresignedUploadRequest("image/jpeg", 1000));

        InOrder order = inOrder(guestQuotaService, imageRepository);
        order.verify(guestQuotaService).reserveUploadSlot(eventId, guestId, 10);
        order.verify(imageRepository).save(any(Image.class));
        assertThat(response.uploadUrl())
                .contains("test-bucket")
                .contains("originals/" + eventId + "/" + response.imageId());
    }

    @Test
    void signedUrlOnlyCreatesThePhotoSoItCannotReplaceIt() {
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(UUID.randomUUID());
        when(guestEventService.getOpenEvent("abc")).thenReturn(event);
        when(imageRepository.save(any(Image.class))).thenAnswer(call -> call.getArgument(0));

        PresignedUploadResponse response = imageUploadService.createPresignedUpload("abc", guestId, new PresignedUploadRequest("image/jpeg", 1000));

        // S3 refuses the PUT with 412 once the object exists (conditional write).
        assertThat(response.uploadUrl()).containsPattern("X-Amz-SignedHeaders=[^&]*if-none-match");
    }

    @Test
    void closedEventReservesNothing() {
        when(guestEventService.getOpenEvent("abc"))
                .thenThrow(new ConflictException(ErrorCode.EVENT_CLOSED, "closed"));

        assertThatThrownBy(() -> imageUploadService.createPresignedUpload("abc", guestId, new PresignedUploadRequest("image/jpeg", 1000)))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(guestQuotaService, imageRepository);
    }
}
