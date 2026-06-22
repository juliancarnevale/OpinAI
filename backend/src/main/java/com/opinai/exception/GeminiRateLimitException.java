package com.opinai.exception;

public class GeminiRateLimitException extends GeminiException {
    public GeminiRateLimitException(String message) {
        super(message);
    }
}
