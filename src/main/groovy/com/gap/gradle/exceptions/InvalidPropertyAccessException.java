package com.gap.gradle.exceptions;

public class InvalidPropertyAccessException extends RuntimeException {
    public InvalidPropertyAccessException(Object message) {
        super(message.toString());
    }
}
