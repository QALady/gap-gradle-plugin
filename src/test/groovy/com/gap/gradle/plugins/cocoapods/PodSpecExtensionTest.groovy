package com.gap.gradle.plugins.cocoapods

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.testng.Assert.assertEquals

public class PodSpecExtensionTest {

    private Project project
    private PodSpecExtension podspec

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()

        podspec = new PodSpecExtension(project)
    }

    @Test
    public void supportsPropertiesAsClosure() throws Exception {
        podspec.name = { "someName" }
        podspec.version = { "someVersion" }

        assertEquals(podspec.name, "someName")
        assertEquals(podspec.version, "someVersion")
    }

    @Test
    public void supportsPropertiesAsString() throws Exception {
        podspec.name = "someName"
        podspec.version = "someVersion"

        assertEquals(podspec.name, "someName")
        assertEquals(podspec.version, "someVersion")
    }

    @Test
    public void usesProjecVersionAsPodVersion() throws Exception {
        project.version = "1.0.0"

        assertEquals(podspec.version, "1.0.0")
    }
}
