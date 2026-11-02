package com.picturepov.api.image;

import com.picturepov.api.common.error.BadRequestException;
import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.event.Event;
import com.picturepov.api.event.EventService;
import com.picturepov.api.guest.GuestLimitService;
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

    private final EventService eventService;
    private final GuestLimitService guestLimitService;
    private final ImageRepository imageRepository;
    private final S3Presigner s3Presigner;
    private final String bucket;
    private final long maxSizeBytes;
    private final Duration uploadUrlTtl;

    ImageUploadService(EventService eventService,
                       GuestLimitService guestLimitService,
                       ImageRepository imageRepository,
                       S3Presigner s3Presigner,
                       @Value("${app.photos.bucket}") String bucket,
                       @Value("${app.photos.max-size-bytes}") long maxSizeBytes,
                       @Value("${app.photos.upload-url-ttl}") Duration uploadUrlTtl) {
        this.eventService = eventService;
        this.guestLimitService = guestLimitService;
        this.imageRepository = imageRepository;
        this.s3Presigner = s3Presigner;
        this.bucket = bucket;
        this.maxSizeBytes = maxSizeBytes;
        this.uploadUrlTtl = uploadUrlTtl;
    }

    @Transactional
    UploadResponse createUpload(String publicCode, UUID guestId, UploadRequest request) {
        validateFileFormat(request);

        Event event = eventService.getActiveEvent(publicCode);
        guestLimitService.reserveUpload(event.getId(), guestId, event.getImagesPerUser());

        Image image = imageRepository.save(
                Image.pending(event.getId(), guestId, request.contentType(), request.sizeBytes()));

        PutObjectRequest putObject = PutObjectRequest.builder()
                .bucket(bucket)
                .key(image.getS3Key())
                .contentType(image.getContentType())
                .contentLength(image.getSizeBytes())
                .build();

        PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(presign -> presign
                .signatureDuration(uploadUrlTtl)
                .putObjectRequest(putObject));

        return new UploadResponse(image.getId(), presigned.url().toString(), presigned.expiration());
    }

    private void validateFileFormat(UploadRequest request) {
        if (!isAllowedType(request.contentType())) {
            throw new BadRequestException(ErrorCode.UNSUPPORTED_CONTENT_TYPE, "Only JPEG, PNG, WebP or HEIC images are allowed");
        }
        if (request.sizeBytes() < 1 || request.sizeBytes() > maxSizeBytes) {
            throw new BadRequestException(ErrorCode.FILE_TOO_LARGE, "Image size must be between 1 byte and " + maxSizeBytes + " bytes");
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
