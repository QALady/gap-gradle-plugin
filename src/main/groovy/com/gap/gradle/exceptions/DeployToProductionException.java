package com.gap.gradle.exceptions;

public class DeployToProductionException extends RuntimeException {
    public DeployToProductionException(Object message, Throwable cause) {
        super((String) message, cause);
    }
}
