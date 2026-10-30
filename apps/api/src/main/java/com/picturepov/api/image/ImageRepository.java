package com.picturepov.api.image;


import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface ImageRepository extends JpaRepository<Image, UUID> {
}
