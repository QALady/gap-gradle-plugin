package com.gap.gradle.plugins.appium

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

/**
 * Created by ra7r7ve on 3/9/15.
 */

class AppiumPluginExtensionTest  {

    private extension
    private project
    private File projectDir

    @Before
    public void setUp() throws Exception {
        projectDir = new File(System.getProperty("java.io.tmpdir"))
        project = ProjectBuilder.builder().withProjectDir(projectDir).build()
        extension = new AppiumPluginExtension(project)
    }

    @Test
    public void shouldHaveDefaultServerArguments(){
        assert(extension.defaultServerFlags == " --log-no-colors --log-timestamp --log ${projectDir.canonicalPath}/build/test/logs/appium.log")
    }

    @Test
    public void shouldAppiumServerArgumentsWithoutExtendedArguments(){
        assert(extension.appiumServerArguments() == " --log-no-colors --log-timestamp --log ${projectDir.canonicalPath}/build/test/logs/appium.log")
    }

    @Test
    public void shouldAppiumServerArgumentsWithExtendedArguments(){

        extension.setExtendedServerFlags("--localtime")
        assert(extension.appiumServerArguments() == " --log-no-colors --log-timestamp --log ${projectDir.canonicalPath}/build/test/logs/appium.log --localtime")
    }

}
