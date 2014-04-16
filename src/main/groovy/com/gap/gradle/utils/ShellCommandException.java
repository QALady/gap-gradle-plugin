package com.gap.gradle.utils;

public class ShellCommandException extends RuntimeException {
    public ShellCommandException(Object message) {
        super(message.toString());
    }
}
