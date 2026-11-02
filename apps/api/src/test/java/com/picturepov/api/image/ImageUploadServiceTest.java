package com.picturepov.api.image;

import com.picturepov.api.common.error.BadRequestException;
import com.picturepov.api.event.EventService;
import com.picturepov.api.guest.GuestLimitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ImageUploadServiceTest {

    @Mock EventService eventService;
    @Mock GuestLimitService guestLimitService;
    @Mock ImageRepository imageRepository;
    @Mock S3Presigner s3Presigner;

    ImageUploadService imageUploadService;

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

}
