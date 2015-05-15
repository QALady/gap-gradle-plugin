package com.gap.gradle.exceptions;

public class SonarRunnerUndefinedProjectVersionException extends RuntimeException{
    public SonarRunnerUndefinedProjectVersionException(Object message) {
        super((String) message);
    }

}
