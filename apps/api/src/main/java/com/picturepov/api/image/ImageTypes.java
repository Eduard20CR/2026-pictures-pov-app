package com.picturepov.api.image;

import java.util.Map;

/** The photo types a guest may upload, with the extension a download gets (originals are stored without one). Copied in apps/zip-export/src/zip.ts: keep both in sync. */
final class ImageTypes {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/heic", ".heic");

    private ImageTypes() {}

    static boolean isAllowed(String contentType) {
        return contentType != null && EXTENSIONS.containsKey(contentType);
    }

    static String extension(String contentType) {
        return EXTENSIONS.getOrDefault(contentType, "");
    }
}
