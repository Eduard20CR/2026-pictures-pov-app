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