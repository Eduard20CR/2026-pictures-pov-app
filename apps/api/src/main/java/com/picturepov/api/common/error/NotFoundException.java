package com.picturepov.api.common.error;

public class NotFoundException extends RuntimeException {
    private final ErrorCode code;

    public NotFoundException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode getCode() { return code; }
}
