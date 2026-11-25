package com.picturepov.api.image;

import com.picturepov.api.event.EventAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/** The review: every photo of an event, hidden ones included (RF-ORG-10, RF-MOD-02). */
@Service
class ReviewImagesService {

    private final EventAccess eventAccess;
    private final ImageRepository imageRepository;
    private final PhotoLinkSigner photoLinkSigner;
    private final Duration thumbnailUrlTtl;

    ReviewImagesService(EventAccess eventAccess,
                        ImageRepository imageRepository,
                        PhotoLinkSigner photoLinkSigner,
                        @Value("${app.photos.thumbnail-url-ttl}") Duration thumbnailUrlTtl) {
        this.eventAccess = eventAccess;
        this.imageRepository = imageRepository;
        this.photoLinkSigner = photoLinkSigner;
        this.thumbnailUrlTtl = thumbnailUrlTtl;
    }

    /** cursor is null for the first page. */
    @Transactional(readOnly = true)
    ReviewImagePage listImages(UUID eventId, UUID userId, int limit, String cursor) {
        ImageCursor after = cursor == null ? null : ImageCursor.decode(cursor);
        eventAccess.getUserRoleInEvent(eventId, userId);

        // One extra row says whether another page follows.
        Limit fetch = Limit.of(limit + 1);
        List<Image> images = after == null
                ? imageRepository.findByEventIdOrderByCreatedAtDescIdDesc(eventId, fetch)
                : imageRepository.findPageAfter(eventId, after.createdAt(), after.id(), fetch);

        List<Image> page = images.subList(0, Math.min(limit, images.size()));
        String nextCursor = images.size() > limit ? ImageCursor.after(page.getLast()).encode() : null;
        List<ReviewImageResponse> items = page.stream()
                .map(image -> new ReviewImageResponse(image.getId(), thumbnailUrl(image), image.isHidden(), image.getCreatedAt()))
                .toList();
        return new ReviewImagePage(items, nextCursor);
    }

    /** null while the thumbnail is not generated yet. */
    private String thumbnailUrl(Image image) {
        if (image.getThumbKey() == null) {
            return null;
        }
        return photoLinkSigner.viewLink(image.getThumbKey(), thumbnailUrlTtl);
    }
}
