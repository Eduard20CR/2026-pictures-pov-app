package com.picturepov.api.image;

/** Processing state: whether the thumbnail exists yet. Hiding is separate ({@link Image#isHidden()}). */
public enum ImageStatus {
    PENDING,
    READY
}