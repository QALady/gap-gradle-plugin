package com.gap.gradle.plugins.cocoapods

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.testng.Assert.assertEquals

public class PodSpecExtensionTest {

    private Project project
    private PodSpecExtension extension

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()

        extension = new PodSpecExtension(project)
    }

    @Test
    public void supportsPropertiesAsClosure() throws Exception {
        extension.podName = { "someName" }
        extension.podVersion = { "someVersion" }

        assertEquals(extension.podName, "someName")
        assertEquals(extension.podVersion, "someVersion")
    }

    @Test
    public void supportsPropertiesAsString() throws Exception {
        extension.podName = "someName"
        extension.podVersion = "someVersion"

        assertEquals(extension.podName, "someName")
        assertEquals(extension.podVersion, "someVersion")
    }

    @Test
    public void usesProjecVersionIfPodVersionNotSpecified() throws Exception {
        project.version = "1.0.0"

        assertEquals(extension.podVersion, "1.0.0")
    }
}
