package com.gap.gradle.plugins.appium

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class AppiumPluginExtensionTest  {

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
    public void shouldHaveDefaultServerArguments(){
        assert(extension.appiumServerArguments() == "--session-override --log-no-colors --log-timestamp --log ${projectDir.canonicalPath}/build/test/logs/appium.log")
    }

    @Test
    public void shouldSupportExtendingDefaultArguments(){
        extension.setExtendedServerFlags("--localtime")

        assert(extension.appiumServerArguments() == "--session-override --log-no-colors --log-timestamp --log ${projectDir.canonicalPath}/build/test/logs/appium.log --localtime")
    }
}
