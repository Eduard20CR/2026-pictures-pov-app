package com.picturepov.api.common.error;

import io.swagger.v3.oas.annotations.media.Schema;

import java.net.URI;
import java.util.List;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

/**
 * OpenAPI shape of every error body (ADR 0016): RFC 9457 plus {@code code} and, for validation, {@code errors}.
 * Documentation only: {@link GlobalExceptionHandler} writes the body as a {@code ProblemDetail}.
 */
@Schema(name = "ApiProblem")
public record ApiProblem(
        @Schema(requiredMode = REQUIRED) URI type,
        @Schema(requiredMode = REQUIRED) String title,
        @Schema(requiredMode = REQUIRED) int status,
        String detail,
        URI instance,
        @Schema(requiredMode = REQUIRED) ErrorCode code,
        @Schema(description = "Invalid fields; only with VALIDATION_FAILED") List<FieldError> errors) {

    public record FieldError(
            @Schema(requiredMode = REQUIRED) String field,
            @Schema(requiredMode = REQUIRED) String message) {
    }
}
