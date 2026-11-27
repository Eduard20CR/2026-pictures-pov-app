package com.picturepov.api.image;

import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Instant;
import java.util.UUID;

/** Photos and a real link signer for unit tests: signing a URL is local math, it never calls AWS. */
public final class ImageFixtures {

    public static final String BUCKET = "test-bucket";

    private ImageFixtures() {}

    public static S3Presigner presigner() {
        return S3Presigner.builder()
                .region(Region.US_EAST_1)
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create("test", "test")))
                .build();
    }

    public static PhotoLinkSigner photoLinkSigner() {
        return new PhotoLinkSigner(presigner(), BUCKET);
    }

    /** A processed JPEG photo of the event, with its thumbnail. */
    static Image readyPhoto(UUID eventId, Instant createdAt) {
        Image image = Image.createPending(eventId, UUID.randomUUID(), "image/jpeg", 1000);
        ReflectionTestUtils.setField(image, "status", ImageStatus.READY);
        ReflectionTestUtils.setField(image, "thumbKey", "thumbs/" + eventId + "/" + image.getId() + ".webp");
        ReflectionTestUtils.setField(image, "createdAt", createdAt);
        return image;
    }

    /** A photo whose thumbnail is not generated yet. */
    static Image processingPhoto(UUID eventId, Instant createdAt) {
        Image image = Image.createPending(eventId, UUID.randomUUID(), "image/jpeg", 1000);
        ReflectionTestUtils.setField(image, "createdAt", createdAt);
        return image;
    }

    static Image hidden(Image image) {
        ReflectionTestUtils.setField(image, "hidden", true);
        return image;
    }
}
