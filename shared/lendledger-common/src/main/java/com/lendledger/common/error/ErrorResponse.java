package com.lendledger.common.error;

public record ErrorResponse(ErrorBody error) {
    public record ErrorBody(String code, String message, String traceId) {}
}
