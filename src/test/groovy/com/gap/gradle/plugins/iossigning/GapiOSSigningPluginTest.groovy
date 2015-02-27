package com.gap.gradle.plugins.iossigning

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static helpers.Assert.taskShouldExist

class GapiOSSigningPluginTest {

    @Test
    public void shouldAddIpaSigningExtensionToProject() throws Exception {
        def project = new ProjectBuilder().build()
        def plugin = new GapiOSSigningPlugin()

        plugin.apply(project)

        taskShouldExist('ipaSigning', project)
    }
}
