package com.picturepov.api.event;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/events")
class EventController {

    @GetMapping("/{publicCode}")
    PublicEventResponse getByPublicCode(@PathVariable String publicCode) {
        return new PublicEventResponse(publicCode, "Boda de prueba", "#E11D48", LocalDate.of(2026, 12, 12), 10);
    }

}
