package com.gap.gradle.plugins

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

//NOTE: If you are writing a new plugin, you should write a separate test
class BasicTestsForPluginsThatDoNotHaveTest {
    def plugins = ['gapbuild','gapupload','gapmetrics',/*'gapci'*/]

    @Test
    void shouldBeAbleToApplyThePlugins()
    {
        def project = ProjectBuilder.builder().build()
        plugins.each {plugin ->
            project.apply plugin: plugin
        }
    }


}
