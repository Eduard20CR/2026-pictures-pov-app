package com.picturepov.api.event;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "events")
public class Event {

    @Id
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "color")
    private String color;

    @Column(name = "public_code")
    private String publicCode;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "images_per_user")
    private int imagesPerUser;

    @Column(name = "expected_guests")
    private Integer expectedGuests;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private EventStatus status;

    @Column(name = "created_at")
    private Instant createdAt;

    protected Event() {}

    private static final String DEFAULT_COLOR = "#000000";
    private static final int DEFAULT_IMAGES_PER_USER = 10;

    static Event draft(String name, LocalDate eventDate, String publicCode) {
        Event event = new Event();
        event.id = UUID.randomUUID();
        event.name = name.trim();
        event.eventDate = eventDate;
        event.publicCode = publicCode;
        event.color = DEFAULT_COLOR;
        event.imagesPerUser = DEFAULT_IMAGES_PER_USER;
        event.status = EventStatus.DRAFT;
        event.createdAt = Instant.now();
        return event;
    }

    public UUID getId() { return id; }
    public String getName() { return name; }
    public String getColor() { return color; }
    public String getPublicCode() { return publicCode; }
    public LocalDate getEventDate() { return eventDate; }
    public int getImagesPerUser() { return imagesPerUser; }
    public Integer getExpectedGuests() { return expectedGuests; }
    public EventStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}