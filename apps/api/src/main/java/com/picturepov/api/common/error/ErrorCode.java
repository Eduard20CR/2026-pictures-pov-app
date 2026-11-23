package com.picturepov.api.common.error;

public enum ErrorCode {
    EVENT_NOT_FOUND,
    UPLOAD_LIMIT_REACHED,
    UNSUPPORTED_CONTENT_TYPE,
    FILE_TOO_LARGE,
    INVALID_FILE_SIZE,
    EVENT_CLOSED,
    MALFORMED_REQUEST_BODY,
    VALIDATION_FAILED,
    INVITATION_ALREADY_EXISTS,
    // Generic codes, for errors without a specific one.
    BAD_REQUEST,
    UNAUTHENTICATED,
    FORBIDDEN,
    NOT_FOUND,
    METHOD_NOT_ALLOWED,
    UNSUPPORTED_MEDIA_TYPE,
    INTERNAL_ERROR;

    /** The generic code for an HTTP status. */
    public static ErrorCode forStatus(int status) {
        return switch (status) {
            case 401 -> UNAUTHENTICATED;
            case 403 -> FORBIDDEN;
            case 404 -> NOT_FOUND;
            case 405 -> METHOD_NOT_ALLOWED;
            case 415 -> UNSUPPORTED_MEDIA_TYPE;
            default -> status >= 500 ? INTERNAL_ERROR : BAD_REQUEST;
        };
    }
}
