package com.gap.gradle.plugins.cocoapods

import org.gradle.api.GradleException
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

public class PodspecValidatorTest {
    private podspec = new FakePodspec()
    private podspecValidator = new PodspecValidator()

    @Test
    public void throwsExepctionIfPodNameNotDefined() throws Exception {
        podspec.name = null

        try {
            podspecValidator.validate(podspec)

            fail("Exception not thrown")
        } catch (GradleException e) {
            assertThat(e.message, containsString("podspec.name"))
        }
    }

    @Test
    public void throwsExepctionIfPodVersionNotDefined() throws Exception {
        podspec.version = null

        try {
            podspecValidator.validate(podspec)

            fail("Exception not thrown")
        } catch (GradleException e) {
            assertThat(e.message, containsString("podspec.version"))
            assertThat(e.message, containsString("project.version"))
        }
    }

    @Test
    public void throwsExepctionIfSourceLocationNotDefined() throws Exception {
        podspec.sourceLocation = null

        try {
            podspecValidator.validate(podspec)

            fail("Exception not thrown")
        } catch (GradleException e) {
            assertThat(e.message, containsString("podspec.sourceLocation"))
        }
    }
}

private class FakePodspec implements Podspec {
    String name = "FakePod"
    String version = "FakeVersion"
    String sourceLocation = "FakeUrl"
}
