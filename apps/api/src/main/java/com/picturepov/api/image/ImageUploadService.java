package com.picturepov.api.image;

import com.picturepov.api.common.error.BadRequestException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.event.Event;
import com.picturepov.api.event.GuestEventService;
import com.picturepov.api.guest.GuestQuotaService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
class ImageUploadService {

    private final GuestEventService guestEventService;
    private final GuestQuotaService guestQuotaService;
    private final ImageRepository imageRepository;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final long maxSizeBytes;
    private final Duration uploadUrlTtl;

    ImageUploadService(GuestEventService guestEventService,
                       GuestQuotaService guestQuotaService,
                       ImageRepository imageRepository,
                       S3Presigner s3Presigner,
                       @Value("${app.photos.bucket}") String bucket,
                       @Value("${app.photos.max-size-bytes}") long maxSizeBytes,
                       @Value("${app.photos.upload-url-ttl}") Duration uploadUrlTtl) {
        this.guestEventService = guestEventService;
        this.guestQuotaService = guestQuotaService;
        this.imageRepository = imageRepository;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.maxSizeBytes = maxSizeBytes;
        this.uploadUrlTtl = uploadUrlTtl;
    }

    @Transactional
    PresignedUploadResponse createPresignedUpload(String publicCode, UUID guestId, PresignedUploadRequest request) {
        validateFileFormat(request);

        Event event = guestEventService.getOpenEvent(publicCode);
        guestQuotaService.reserveUploadSlot(event.getId(), guestId, event.getImagesPerUser());

        Image image = imageRepository.save(
                Image.createPending(event.getId(), guestId, request.contentType(), request.sizeBytes()));

        PutObjectRequest putObject = PutObjectRequest.builder()
                .bucket(bucket)
                .key(image.getS3Key())
                .contentType(image.getContentType())
                .contentLength(image.getSizeBytes())
                // Create-only: once the photo exists, S3 rejects a reused URL with 412.
                .ifNoneMatch("*")
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presign -> presign
                .signatureDuration(uploadUrlTtl)
                .putObjectRequest(putObject));

        return new PresignedUploadResponse(image.getId(), presigned.url().toString(), presigned.expiration());
    }

    private void validateFileFormat(PresignedUploadRequest request) {
        if (!isAllowedType(request.contentType())) {
            throw new BadRequestException(ErrorCode.UNSUPPORTED_CONTENT_TYPE, "Only JPEG, PNG, WebP or HEIC images are allowed");
        }
        if (request.sizeBytes() < 1) {
            throw new BadRequestException(ErrorCode.INVALID_FILE_SIZE, "Image size must be at least 1 byte");
        }
        if (request.sizeBytes() > maxSizeBytes) {
            throw new BadRequestException(ErrorCode.FILE_TOO_LARGE, "Image size must be at most " + maxSizeBytes + " bytes");
        }
    }

    private static boolean isAllowedType(String contentType) {
        if (contentType == null) {
            return false;
        }
        return switch (contentType) {
            case "image/jpeg", "image/png", "image/webp", "image/heic" -> true;
            default -> false;
        };
    }

}
