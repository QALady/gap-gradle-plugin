package com.gap.gradle.plugins

import static helpers.Assert.shouldExecuteTask
import static helpers.Assert.taskShouldExist
import static junit.framework.Assert.assertTrue
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.SonarLinkTask
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GapSonarRunnerPluginTest {
    private Project project

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build()
    }

    @Test
    public void shouldApplySonarRunnerPlugin() {
        project.apply plugin: 'gap-sonar-runner'
        assertThat(project.plugins.hasPlugin('sonar-runner'), is(true))
    }

    @Test
    public void shouldApplyJaCoCoPlugin() {
        project.apply plugin: 'gap-sonar-runner'
        assertThat(project.plugins.hasPlugin('jacoco'), is(true))
    }

    @Test
    public void shouldAddJaCoCoTasks() {
        project.apply plugin: 'java'
        project.apply plugin: 'gap-sonar-runner'
        taskShouldExist('jacocoTestReport', project)
    }


    @Test
    void sonarTaskShouldBeAddedToProject() {
        project.apply plugin: 'gap-sonar-runner'
        taskShouldExist('sonar', project)
    }

    @Test
    void shouldExecuteSonarLinkTask() {
        project.apply plugin: 'gap-sonar-runner'
        shouldExecuteTask(project, 'sonar', SonarLinkTask)
    }

    @Test
    void sonar_shouldDependOnSonarRunner() {
        project.apply plugin: 'gap-sonar-runner'
        assertTrue(project.tasks.sonar.dependsOn.contains('sonarRunner'))
    }

    @Test
    void sonar_shouldRunInPreviewModeIfRunningOutsideEC() {
        def commanderMock = new MockFor(CommanderClient)
        commanderMock.demand.isRunningInPipeline {
            false
        }
        commanderMock.use {
            project.apply plugin: 'gap-sonar-runner'
            assertThat(project.tasks.findByName('sonarRunner').sonarProperties.getProperty('sonar.analysis.mode'), is('preview'))
        }
    }

    @Test
    void sonar_shouldRunWithNextDateInLocal() {
        def commanderMock = new MockFor(CommanderClient)
        def nextDay = new Date().next()
        commanderMock.demand.isRunningInPipeline {
            false
        }
        commanderMock.use {
            project.apply plugin: 'gap-sonar-runner'
            assertThat(project.tasks.findByName('sonarRunner').sonarProperties.getProperty('sonar.projectDate'), is(nextDay.format('YYYY-MM-dd')))
        }
    }

    @Test
    void sonar_shouldRunInAnalysisModeIfRunningInEC() {
        def commanderMock = new MockFor(CommanderClient)
        commanderMock.demand.isRunningInPipeline() {
            true
        }
        commanderMock.use {
            project.apply plugin: 'gap-sonar-runner'
            assertThat(project.tasks.findByName('sonarRunner').sonarProperties.getProperty('sonar.analysis.mode'), is('analysis'))
        }
    }
}
