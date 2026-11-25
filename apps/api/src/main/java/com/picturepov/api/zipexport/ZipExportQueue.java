package com.picturepov.api.zipexport;

import java.util.UUID;

/** Hands an export to the ZIP worker (apps/zip-export, ADR 0008). */
interface ZipExportQueue {
    void send(UUID exportId);
}
