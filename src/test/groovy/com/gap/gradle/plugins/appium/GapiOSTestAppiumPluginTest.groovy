package com.gap.gradle.plugins.appium

import com.gap.gradle.tasks.StartBackgroundProcessTask
import com.gap.gradle.tasks.StopProcessByPidTask
import com.gap.gradle.utils.FileDownloader
import groovy.mock.interceptor.MockFor
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static com.gap.gradle.plugins.appium.GapiOSTestAppiumPlugin.DEFAULT_INSTRUMENTS_TEMPLATE_URI
import static helpers.Assert.*
import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

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
        taskShouldBeOfType('stopAppium', StopProcessByPidTask, project)

        taskShouldExist('configureAppiumForRealDevices', project)

        taskShouldExist('startAppium', project)
        taskShouldDependOn('startAppium', 'startiOSWebkitDebugProxy', project)
        taskShouldBeOfType('startAppium', StartBackgroundProcessTask, project)

        taskShouldExist('startiOSWebkitDebugProxy', project)
        taskShouldBeOfType('startiOSWebkitDebugProxy', StartBackgroundProcessTask, project)

        taskShouldExist('zipInstrumentsTraceResults', project)
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

    @Test
    public void shouldUseDefaultTemplateIfNoOtherWasSpecified() throws Exception {
        project.appiumConfig {
            simulatorMode = false
        }

        fakeDownloader.use {
            project.tasks.configureAppiumForRealDevices.execute()
        }

        String serverArguments = project.appiumConfig.appiumServerArguments()

        assertThat(serverArguments, containsString("--tracetemplate ${defaultTemplateMock.absolutePath}"))
    }

    @Test
    public void shouldSupportAlternativeTemplates() throws Exception {
        project.appiumConfig {
            simulatorMode = false
            instrumentsTemplateURI "non-standard-template"
        }

        fakeDownloader.use {
            project.tasks.configureAppiumForRealDevices.execute()
        }

        String serverArguments = project.appiumConfig.appiumServerArguments()

        assertThat(serverArguments, containsString("--tracetemplate ${alternateTemplateMock.absolutePath}"))
    }

    def static getFakeDownloader() {
        def fakeDownloader = new MockFor(FileDownloader)

        fakeDownloader.demand.download { String src, File dest ->
            return src.equals(DEFAULT_INSTRUMENTS_TEMPLATE_URI) ? defaultTemplateMock : alternateTemplateMock
        }

        fakeDownloader
    }

    private static File getDefaultTemplateMock() {
        fakeFileWithAbsolutePath("/path/to/default/template")
    }

    private static File getAlternateTemplateMock() {
        fakeFileWithAbsolutePath("/path/to/some/file")
    }

    def static fakeFileWithAbsolutePath(String path) {
        def fakeFile = mock(File)

        when(fakeFile.absolutePath).thenReturn(path)

        fakeFile
    }
}
