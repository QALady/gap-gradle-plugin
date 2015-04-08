package com.gap.gradle.plugins.cocoapods

import org.gradle.api.GradleException
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

public class PodspecValidatorTest {
    private podspec = new FakePodspec()

    @Test
    public void throwsExepctionIfPodNameNotDefined() throws Exception {
        podspec.podName = null

        try {
            PodspecValidator.validate(podspec)

            fail("Exception not thrown")
        } catch (GradleException e) {
            assertThat(e.message, containsString("podName"))
        }
    }

    @Test
    public void throwsExepctionIfPodVersionNotDefined() throws Exception {
        podspec.podVersion = null

        try {
            PodspecValidator.validate(podspec)

            fail("Exception not thrown")
        } catch (GradleException e) {
            assertThat(e.message, containsString("podVersion"))
        }
    }
}

private class FakePodspec implements Podspec {
    String podName = "FakePod"
    String podVersion = "FakeVersion"
}
