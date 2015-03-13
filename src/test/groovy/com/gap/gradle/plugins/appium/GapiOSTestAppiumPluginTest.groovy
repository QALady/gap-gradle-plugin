package com.gap.gradle.plugins.appium

import com.gap.gradle.tasks.SpawnBackgroundProcessTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static helpers.Assert.taskShouldExist
import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.Matchers.instanceOf
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

class GapiOSTestAppiumPluginTest {

    private Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()

        project.apply plugin: 'gap-ios-appium'
    }

    @Test
    public void shouldAddStopAppiumTask() {
        project.apply plugin: 'gap-ios-appium'
        taskShouldExist('stopAppium', project)
    }

    @Test
    public void shouldAddStartAppiumTask() {
        taskShouldExist('startAppium', project)
    }

    @Test
    public void shouldStartAppiumTaskWithServerArgs() {
        project.appiumConfig {
            extendedServerFlags 'extraParamaters --forserver'
        }

        assertThat(project.tasks.startAppium, instanceOf(SpawnBackgroundProcessTask))
    }

    @Test
    public void shouldSkipiOSDebugProxyWithDefaulSimulatedMode() {
        project.tasks.startiOSWebkitDebugProxy.execute()
        assertTrue(project.tasks.startiOSWebkitDebugProxy.state.skipped)

    }

    @Test
    public void shouldAddGetPerfMetricsTask() {
        taskShouldExist('getPerfMetrics', project)
    }

    @Test
    public void shouldThrowExceptionIfDeviceNotConnected() {
        project.appiumConfig {
            simulatorMode = false
        }

        try {
            project.tasks.startiOSWebkitDebugProxy.execute()
        } catch (GradleException e) {
            assertThat(e.cause.message, containsString('No iPod device detected'))
        }
    }
}
