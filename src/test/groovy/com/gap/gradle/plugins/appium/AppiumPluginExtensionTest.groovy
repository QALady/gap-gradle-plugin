package com.gap.gradle.plugins.appium

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.testng.Assert.assertEquals

class AppiumPluginExtensionTest {

    private AppiumPluginExtension extension
    private Project project
    private String logFilePath

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()
        extension = new AppiumPluginExtension(project)
        logFilePath = "${project.buildDir}/test/logs/appium.log"
    }

    @Test
    public void shouldHaveDefaultServerArguments() {
        assertEquals(extension.appiumServerArguments(), "--session-override --log-no-colors --log-timestamp &> $logFilePath")
    }

    @Test
    public void shouldKeepLogFileRedirectAsLastArgument() {
        extension.setExtendedServerFlags("--some-arg")

        assertEquals(extension.appiumServerArguments(), "--session-override --log-no-colors --log-timestamp --some-arg &> $logFilePath")
    }
}
