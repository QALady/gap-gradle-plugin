package com.gap.gradle.plugins.xcode

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.instanceOf
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat

public class XcodeExtensionTest {

    private XcodeExtension extension

    @Before
    public void setUp() throws Exception {
        Project project = ProjectBuilder.builder().build()
        Instantiator instantiator = ((ProjectInternal) project).getServices().get(Instantiator)

        extension = new XcodeExtension(instantiator, project)
    }

    @Test
    public void shouldInitializeConfigContainers() throws Exception {
        assertThat(extension.test, instanceOf(XcodeTestConfig))
        assertThat(extension.build, instanceOf(XcodeBuildConfig))
        assertThat(extension.archive, instanceOf(XcodeArchiveConfig))
    }

    @Test
    public void shouldReturnTrueIfTargetIsNeeded() throws Exception {
        assertThat(extension.isTargetRequired(), CoreMatchers.is(true))
    }

    @Test
    public void shouldReturnNullIfWorkspaceWasNotSpecified() throws Exception {
        assertNull(extension.workspace)
    }

    @Test
    public void shouldReturnNullIfSchemeWasNotDefined() throws Exception {
        assertNull(extension.scheme)
    }
}
