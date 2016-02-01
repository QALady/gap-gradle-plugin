package com.gap.gradle.plugins

import org.gradle.api.Project
import org.junit.Before

import static org.junit.Assert.assertThat
import org.gradle.testfixtures.ProjectBuilder

import static org.hamcrest.CoreMatchers.notNullValue
import org.junit.Test
import org.junit.Ignore

import static org.hamcrest.CoreMatchers.is

@Ignore
class GapUploadPluginTest {

    Project project

    @Before
    public void setUp(){
        project = ProjectBuilder.builder().build()
    }

    @Test
    public void shouldAddPackageTasks_whenJavaPluginIsApplied(){
        project.apply plugin: 'java'
        project.apply plugin: 'gapupload'
        taskShouldExist('packageConfigs')
        taskShouldExist('packageTests')
        taskShouldExist('packageIntegrationTests')
        taskShouldExist('packageSmokeTests')
    }

    @Test
    public void shouldAddPackageArtifactsToRespectiveConfigurations(){
        project.apply plugin: 'java'
        project.apply plugin: 'gapupload'
        assertThat(numberOfArtifactsInConfiguration('config'), is(1))
        assertThat(numberOfArtifactsInConfiguration('testRuntime'), is(1))
        assertThat(numberOfArtifactsInConfiguration('integrationTest'), is(1))
        assertThat(numberOfArtifactsInConfiguration('smokeTest'), is(1))
    }

    int numberOfArtifactsInConfiguration(String configuration) {
        project.configurations.getByName(configuration).artifacts.size()
    }


    def taskShouldExist(task) {
        assertThat(project.tasks.findByName(task), notNullValue())
    }
}
