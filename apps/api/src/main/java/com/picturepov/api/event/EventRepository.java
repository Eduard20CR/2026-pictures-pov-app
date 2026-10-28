package com.picturepov.api.event;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

interface EventRepository extends JpaRepository<Event, UUID> {
    Optional<Event> findByPublicCode(String publicCode);
}