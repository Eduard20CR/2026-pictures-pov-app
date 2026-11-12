package com.picturepov.api.image;

import com.picturepov.api.common.error.BadRequestException;
import com.picturepov.api.common.error.ConflictException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.event.Event;
import com.picturepov.api.event.EventService;
import com.picturepov.api.guest.GuestLimitService;
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

    @Mock EventService eventService;
    @Mock GuestLimitService guestLimitService;
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
                eventService, guestLimitService, imageRepository, s3Presigner,
                "test-bucket", 20_971_520L, Duration.ofMinutes(5));
    }

    @Test
    void rejectsUnsupportedContentType(){
        assertThatThrownBy(()->imageUploadService.createUpload("abc", UUID.randomUUID(), new UploadRequest("image/gif", 1000)))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(guestLimitService);
    }

    @Test
    void rejectsFileLargerThanLimit(){
        assertThatThrownBy(()->imageUploadService.createUpload("abc", UUID.randomUUID(), new UploadRequest("image/jpeg", 20_971_521L)))
                .isInstanceOf(BadRequestException.class);
        verifyNoInteractions(guestLimitService);
    }

    @Test
    void reservesQuotaThenReturnsASignedUrlForThePhoto() {
        UUID eventId = UUID.randomUUID();
        Event event = mock(Event.class);
        when(event.getId()).thenReturn(eventId);
        when(event.getImagesPerUser()).thenReturn(10);
        when(eventService.getActiveEvent("abc")).thenReturn(event);
        when(imageRepository.save(any(Image.class))).thenAnswer(call -> call.getArgument(0));

        UploadResponse response = imageUploadService.createUpload("abc", guestId, new UploadRequest("image/jpeg", 1000));

        InOrder order = inOrder(guestLimitService, imageRepository);
        order.verify(guestLimitService).reserveUpload(eventId, guestId, 10);
        order.verify(imageRepository).save(any(Image.class));
        assertThat(response.uploadUrl())
                .contains("test-bucket")
                .contains("originals/" + eventId + "/" + response.imageId());
    }

    @Test
    void closedEventReservesNothing() {
        when(eventService.getActiveEvent("abc"))
                .thenThrow(new ConflictException(ErrorCode.EVENT_CLOSED, "closed"));

        assertThatThrownBy(() -> imageUploadService.createUpload("abc", guestId, new UploadRequest("image/jpeg", 1000)))
                .isInstanceOf(ConflictException.class);
        verifyNoInteractions(guestLimitService, imageRepository);
    }
}
