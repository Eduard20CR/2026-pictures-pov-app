package com.picturepov.api.event;

import java.time.LocalDate;

public record PublicEventResponse(
        String publicCode,
        String name,
        String color,
        LocalDate eventDate,
        int imagesPerUser
) {}