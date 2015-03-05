package com.gap.gradle.plugins.appium

import com.gap.gradle.tasks.SpawnBackgroundProcessTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

import static helpers.Assert.taskShouldExist
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

class GapiOSTestAppiumPluginTest {

    private Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    public void shouldAddStopAppiumTask() {
        project.apply plugin: 'gap-ios-appium'
        taskShouldExist('stopAppium', project)

    }

    @Test
    public void shouldAddStartAppiumTask() {
        project.apply plugin: 'gap-ios-appium'

        taskShouldExist('startAppium', project)
    }

    @Test
    public void shouldStartAppiumTaskWithServerArgs() {

        project.apply plugin: 'gap-ios-appium'

        project.appiumConfig {
            serverFlags 'extraParamaters --forserver'
        }

        assertThat(project.tasks.startAppium, instanceOf(com.gap.gradle.tasks.SpawnBackgroundProcessTask))

    }

    @Test
    public void shouldSkipiOSDebugProxyWithDefaulSimulatedMode() {
        project.apply plugin: 'gap-ios-appium'
        project.tasks.startiOSWebkitDebugProxy.execute()
        assertTrue(project.tasks.startiOSWebkitDebugProxy.state.skipped)

    }

}
