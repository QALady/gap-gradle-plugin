package com.gap.gradle.plugins.appium

import com.gap.gradle.tasks.SpawnBackgroundProcessTask
import com.gap.gradle.tasks.StopProcessByPortTask
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static helpers.Assert.*
import static org.hamcrest.CoreMatchers.containsString
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
    public void shouldAddNewTasks() {
        taskShouldExist('stopAppium', project)
        taskShouldBeOfType('stopAppium', StopProcessByPortTask, project)

        taskShouldExist('startAppium', project)
        taskShouldBeOfType('startAppium', SpawnBackgroundProcessTask, project)

        taskShouldExist('startiOSWebkitDebugProxy', project)
        taskShouldBeOfType('startiOSWebkitDebugProxy', SpawnBackgroundProcessTask, project)

        taskShouldExist('startAppiumForPerformanceTests', project)

        ['startAppium', 'startAppiumForPerformanceTests'].each {
            taskShouldDependOn(it, 'startiOSWebkitDebugProxy', project)
        }
    }

    @Test
    public void shouldNotStartDebugProxyInSimulatorMode() {
        project.tasks.startiOSWebkitDebugProxy.execute()

        assertTrue(project.tasks.startiOSWebkitDebugProxy.state.skipped)
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
