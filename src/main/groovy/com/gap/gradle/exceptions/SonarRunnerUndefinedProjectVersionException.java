package com.gap.gradle.exceptions;

public class SonarRunnerUndefinedProjectVersionException extends RuntimeException{
    public SonarRunnerUndefinedProjectVersionException(String message) {
        super(message);
    }

}