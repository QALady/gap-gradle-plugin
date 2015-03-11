package com.gap.gradle.plugins.iossigning.exceptions;

import com.gap.gradle.plugins.airwatch.ArtifactSpec;

import static java.lang.String.format;

public class ArtifactNotFoundException extends RuntimeException {
    public ArtifactNotFoundException(ArtifactSpec artifactSpec) {
        super(format("No artifact to be signed was found in the dependencies with these values: %s", artifactSpec.toStringWithDefinedValues()));
    }
}
