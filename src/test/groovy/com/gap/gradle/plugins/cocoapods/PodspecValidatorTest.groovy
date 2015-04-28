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
        podspec.podName = null

        try {
            podspecValidator.validate(podspec)

            fail("Exception not thrown")
        } catch (GradleException e) {
            assertThat(e.message, containsString("podName"))
        }
    }

    @Test
    public void throwsExepctionIfPodVersionNotDefined() throws Exception {
        podspec.podVersion = null

        try {
            podspecValidator.validate(podspec)

            fail("Exception not thrown")
        } catch (GradleException e) {
            assertThat(e.message, containsString("project.version"))
        }
    }
}

private class FakePodspec implements Podspec {
    String podName = "FakePod"
    String podVersion = "FakeVersion"
}
