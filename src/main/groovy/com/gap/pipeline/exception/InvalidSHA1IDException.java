package com.gap.pipeline.exception;

public class InvalidSHA1IDException extends RuntimeException {
    public InvalidSHA1IDException(Object message) {
        super(message.toString());
    }
}
