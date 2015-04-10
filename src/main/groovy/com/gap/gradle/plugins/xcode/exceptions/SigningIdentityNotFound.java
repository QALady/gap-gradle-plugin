package com.gap.gradle.plugins.xcode.exceptions;

public class SigningIdentityNotFound extends RuntimeException {
    public SigningIdentityNotFound(String message) {
        super(message);
    }
}
