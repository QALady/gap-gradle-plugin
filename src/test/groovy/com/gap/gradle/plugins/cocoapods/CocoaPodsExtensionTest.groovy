package com.gap.gradle.plugins.cocoapods

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.*

public class CocoaPodsExtensionTest {

    private Project project
    private CocoaPodsExtension extension

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()

        extension = new CocoaPodsExtension(project)
    }

    @Test
    public void shouldSupportPropertiesAsClosure() throws Exception {
        extension.podName = { "someName" }
        extension.podVersion = { "someVersion" }

        assertEquals(extension.podName, "someName")
        assertEquals(extension.podVersion, "someVersion")
    }

    @Test
    public void shouldSupportPropertiesAsString() throws Exception {
        extension.podName = "someName"
        extension.podVersion = "someVersion"

        assertEquals(extension.podName, "someName")
        assertEquals(extension.podVersion, "someVersion")
    }

    @Test
    public void throwsExepctionIfPodNameNotDefined() throws Exception {
        try {
            extension.podName

            fail("Exception not thrown")
        } catch (GradleException e) {
            assertThat(e.message, containsString("podName"))
        }
    }

    @Test
    public void throwsExepctionIfPodVersionNotDefined() throws Exception {
        try {
            println extension.podVersion

            fail("Exception not thrown")
        } catch (GradleException e) {
            assertThat(e.message, containsString("podVersion"))
        }
    }
}
