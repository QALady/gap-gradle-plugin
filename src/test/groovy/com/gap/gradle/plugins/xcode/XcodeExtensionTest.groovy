package com.gap.gradle.plugins.xcode

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.DependencyInjectingInstantiator
import org.gradle.internal.service.ServiceRegistry
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.instanceOf
import static org.junit.Assert.assertNull
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock

public class XcodeExtensionTest {

    private XcodeExtension extension

    @Before
    public void setUp() throws Exception {
        final ServiceRegistry services = mock(ServiceRegistry)
        final Action warning = mock(Action)
        final DependencyInjectingInstantiator instantiator = new DependencyInjectingInstantiator(services, warning)

        final Project project = ProjectBuilder.builder().build()

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
