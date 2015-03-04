package com.gap.gradle.plugins.iossigning.exceptions;

import com.gap.gradle.airwatch.ArtifactSpec;
import org.junit.Test;

import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

public class ArtifactNotFoundExceptionTest {
    @Test
    public void shouldGiveUsefulMessageAboutTheException() throws Exception {
        ArtifactSpec artifactSpec = new ArtifactSpec();
        ArtifactNotFoundException exception = new ArtifactNotFoundException(artifactSpec);

        assertThat(exception.getMessage(), containsString(artifactSpec.toStringWithDefinedValues()));
    }
}
