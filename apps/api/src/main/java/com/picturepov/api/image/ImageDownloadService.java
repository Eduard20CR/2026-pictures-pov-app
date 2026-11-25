package com.picturepov.api.image;

import com.picturepov.api.common.error.ErrorCode;
import com.picturepov.api.common.error.NotFoundException;
import com.picturepov.api.event.EventAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.UUID;

/** Downloading one photo's original (RF-ORG-13, RN-08). */
@Service
class ImageDownloadService {

    private final EventAccess eventAccess;
    private final ImageRepository imageRepository;
    private final PhotoLinkSigner photoLinkSigner;
    private final Duration downloadUrlTtl;

    ImageDownloadService(EventAccess eventAccess,
                         ImageRepository imageRepository,
                         PhotoLinkSigner photoLinkSigner,
                         @Value("${app.photos.download-url-ttl}") Duration downloadUrlTtl) {
        this.eventAccess = eventAccess;
        this.imageRepository = imageRepository;
        this.photoLinkSigner = photoLinkSigner;
        this.downloadUrlTtl = downloadUrlTtl;
    }

    /** 404 if the user holds no Accepted invitation or the photo is not the event's or is still processing; 403 for a moderator (RN-05). */
    @Transactional(readOnly = true)
    ImageDownloadResponse downloadLink(UUID eventId, UUID imageId, UUID userId) {
        eventAccess.requireOrganizer(eventId, userId);
        // A photo still processing may not have its original stored yet.
        Image image = imageRepository.findByIdAndEventIdAndStatus(imageId, eventId, ImageStatus.READY)
                .orElseThrow(() -> new NotFoundException(ErrorCode.IMAGE_NOT_FOUND, "Image not found"));
        return new ImageDownloadResponse(photoLinkSigner.downloadLink(image.getS3Key(), fileName(image), downloadUrlTtl));
    }

    private static String fileName(Image image) {
        return image.getId() + ImageTypes.extension(image.getContentType());
    }
}
