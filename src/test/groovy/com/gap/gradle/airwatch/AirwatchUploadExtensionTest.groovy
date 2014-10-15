package com.gap.gradle.airwatch
import org.gradle.api.tasks.Copy
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.mock

public class AirwatchUploadExtensionTest {
    private extension
    private project

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()
        extension = new AirwatchUploadExtension(project, mock(Instantiator), mock(Copy))
    }

    @Test
    public void shouldSupportAppNameAsClosure() throws Exception {
        extension.appName = { "foobar" }

        assertEquals("foobar", extension.appName)
    }

    @Test
    public void shouldSupportAppNameAsString() throws Exception {
        extension.appName = "foobar"

        assertEquals("foobar", extension.appName)
    }

    @Test
    public void shouldSupportAppDescriptionAsClosure() throws Exception {
        extension.appDescription = { "foobar" }

        assertEquals("foobar", extension.appDescription)
    }

    @Test
    public void shouldSupportAppDescriptionAsString() throws Exception {
        extension.appDescription = "foobar"

        assertEquals("foobar", extension.appDescription)
    }
}