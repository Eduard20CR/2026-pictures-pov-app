package com.picturepov.api.zipexport;

import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.UUID;

/** ZIPs in the states the worker leaves them. */
final class ZipExportFixtures {

    private ZipExportFixtures() {}

    static ZipExport zipExport(ZipExportStatus status, Instant createdAt) {
        ZipExport export = ZipExport.createPending(UUID.randomUUID(), UUID.randomUUID());
        ReflectionTestUtils.setField(export, "status", status);
        ReflectionTestUtils.setField(export, "createdAt", createdAt);
        return export;
    }

    /** Stored by the worker with 12 photos in 3.4 MB. */
    static ZipExport readyZipExport() {
        ZipExport export = zipExport(ZipExportStatus.READY, Instant.now());
        ReflectionTestUtils.setField(export, "s3Key", "zips/" + export.getId() + ".zip");
        ReflectionTestUtils.setField(export, "imageCount", 12);
        ReflectionTestUtils.setField(export, "sizeBytes", 3_400_000L);
        ReflectionTestUtils.setField(export, "completedAt", Instant.now());
        return export;
    }
}
