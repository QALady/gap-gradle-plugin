package com.gap.gradle.exceptions;

class DeployToProductionException extends RuntimeException {

    DeployToProductionException(message, cause) {
        super(message, cause)
    }
}
