package com.opinai.exception;

public class ReportGenerationException extends BusinessException {
    public ReportGenerationException(String message, Throwable cause) {
        super(message);
        if (cause != null) {
            this.initCause(cause);
        }
    }
}
