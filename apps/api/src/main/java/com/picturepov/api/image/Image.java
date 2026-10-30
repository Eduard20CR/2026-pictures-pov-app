package com.picturepov.api.image;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "images")
public class Image {

    @Id
    private UUID id;

    @Column(name = "event_id")
    private UUID eventId;

    @Column(name = "s3_key")
    private String s3Key;

    @Column(name = "thumb_key")
    private String thumbKey;

    @Column(name = "content_type")
    private String contentType;

    @Column(name = "size_bytes")
    private long sizeBytes;

    @Column(name = "guest_id")
    private UUID guestId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private ImageStatus status;

    @Column(name = "created_at")
    private Instant createdAt;

    protected Image() {}

    static Image pending(UUID eventId, UUID guestId, String contentType, long sizeBytes) {
        Image image = new Image();
        image.id = UUID.randomUUID();
        image.eventId = eventId;
        image.guestId = guestId;
        image.contentType = contentType;
        image.sizeBytes = sizeBytes;
        image.s3Key = "originals/" + eventId + "/" + image.id;
        image.status = ImageStatus.PENDING;
        image.createdAt = Instant.now();
        return image;
    }

    public UUID getId() { return id; }
    public String getS3Key() { return s3Key; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }
}