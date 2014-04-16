package com.gap.pipeline.exception;

public class MissingParameterException extends RuntimeException {
    public MissingParameterException(Object message) {
        super(message.toString());
    }
}
