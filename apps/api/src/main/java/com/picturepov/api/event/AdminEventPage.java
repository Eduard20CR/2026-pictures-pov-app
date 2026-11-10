package com.picturepov.api.event;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import org.springframework.data.domain.Page;

public record AdminEventPage(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<AdminEventResponse> items,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int page,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int size,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long totalItems,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) int totalPages
) {
    static AdminEventPage from(Page<AdminEventResponse> page) {
        return new AdminEventPage(page.getContent(), page.getNumber(), page.getSize(),
                page.getTotalElements(), page.getTotalPages());
    }
}