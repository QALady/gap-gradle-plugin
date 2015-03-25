package com.gap.gradle.plugins.appium

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.testng.Assert.assertEquals

class AppiumPluginExtensionTest {

    private AppiumPluginExtension extension
    private Project project
    private File projectDir

    @Before
    public void setUp() throws Exception {
        projectDir = new File(System.getProperty("java.io.tmpdir"))
        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        extension = new AppiumPluginExtension(project)
    }

    @Test
    public void shouldHaveDefaultServerArguments() {
        assertEquals("--session-override --log-no-colors --log-timestamp &> ${projectDir.canonicalPath}/build/test/logs/appium.log", extension.appiumServerArguments())
    }

    @Test
    public void shouldSupportExtendingDefaultArguments() {
        extension.setExtendedServerFlags("--localtime")

        assertEquals("--session-override --log-no-colors --log-timestamp &> ${projectDir.canonicalPath}/build/test/logs/appium.log --localtime", extension.appiumServerArguments())
    }
}
