package com.gap.pipeline

import com.gap.pipeline.ec.CommanderArtifacts
import com.gap.pipeline.ec.CommanderClient

import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

import com.gap.pipeline.tasks.*
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GapPipelinePluginTest {

    Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gappipeline'
    }

    @Test
    void ivyIdentifiersTaskShouldBeAddedToProject() {
        taskShouldExist('ivyIdentifiers')
    }

    @Test
    void ivyDependenciesTaskShouldBeAddedToProject() {
        taskShouldExist('ivyDependencies')
    }

    @Test
    void unzipIntegrationTestsTaskShouldBeAddedToProject() {
        taskShouldExist('unzipIntegrationTests')
    }

    @Test
    void prepareForProductionDeployTaskIsAddedToTheProject() {
        taskShouldExist('prepareForProductionDeploy')
    }

    @Test
    void shouldExecuteIvyDependenciesTask() {
        def task = project.tasks.findByName('ivyDependencies')
        task.execute()
    }

    @Test
    void shouldExecuteIvyIdentifiersTask() {
        def task = project.tasks.findByName('ivyIdentifiers')
        task.execute()
    }

    @Test
    void shouldExecuteIvySegmentVersionTask() {
        def task = project.tasks.findByName('ivySegmentVersion')
        task.execute()
    }

    @Test
    void shouldExecutePrepareForProductionDeployTask() {
        def mockTask = new MockFor(PrepareForProductionDeployTask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('prepareForProductionDeploy')
        mockTask.use {
            task.execute()
        }
    }

    @Test
    void validatePrepareForProductionInputTaskIsAddedToTheProject() {
        taskShouldExist('validatePrepareForProductionInput')
    }

    @Test
    void validateGenerateAuditReportTaskIsAddedToTheProject() {
        taskShouldExist('generateAuditReport')
    }

    @Test
    void verifyPrepareForProductionTaskDependencies (){
        taskShouldDependOn('prepareForProductionDeploy', 'validatePrepareForProductionInput')
        taskShouldDependOn('prepareForProductionDeploy', 'generateAuditReport')
        taskShouldDependOn('prepareForProductionDeploy', 'setupBuildDirectories')
    }


    @Test
    void validatePrepareForProductionInput_shouldInvokeValidateOnPrepareForProductionDeployTask() {
        def task = project.tasks.findByName('validatePrepareForProductionInput')
        def mockTask = new MockFor(PrepareForProductionDeployTask)
        mockTask.demand.validate {}
        mockTask.use {
            task.execute()
        }
    }

    @Test
    void shouldExecuteGenerateAuditReportTask() {
        def mockTask = new MockFor(GenerateAuditReportTask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('generateAuditReport')
        mockTask.use {
           task.execute()
        }


    }


    @Test
    void shouldSetProdDeployExtensionPropertiesFromString() {
        def project = ProjectBuilder.builder().build()
        project['prodPrepare.shaId'] = 'git sha id'
        project['prodPrepare.roleName'] = 'role name'
        project['prodPrepare.cookbookName'] = 'cookbook name'
        project.apply plugin: 'gappipeline'
        assertThat(project.prodPrepare.shaId, is('git sha id'))
        assertThat(project.prodPrepare.roleName, is('role name'))
        assertThat(project.prodPrepare.cookbookName, is('cookbook name'))
    }

    @Test
    void shouldSetIvyPropertiesFromString() {
        def project = ProjectBuilder.builder().build()
        project.ext.set('ivy.url','ivy_url')
        project.ext.set('ivy.userName', 'ivy_user')
        project.ext.set('ivy.password', 'ivy_password')
        project.apply plugin: 'gappipeline'
        assertThat(project.ivy.url, is('ivy_url'))
        assertThat(project.ivy.userName, is('ivy_user'))
        assertThat(project.ivy.password, is('ivy_password'))
    }


    @Test
    void setupBuildDirsTaskIsAddedToTheProject (){
        taskShouldExist('setupBuildDirectories')
    }

    @Test
    void shouldExecuteSetupBuildDirectoriesTask (){
        def taskMock = new MockFor(SetUpBuildDirectoriesTask)
        taskMock.demand.execute { }
        taskMock.use {
            project.tasks.getByName('setupBuildDirectories').execute()
        }
    }

    @Test
    void uploadBuildArtifactsTaskIsAddedToTheProject(){
        taskShouldExist('uploadBuildArtifacts')
    }


    @Test
    void downloadArtifactsTaskIsAddedToTheProject(){
        taskShouldExist('downloadArtifacts')
    }

    @Test
    void shouldExecuteUploadBuildArtifactsTask(){
        def taskMock = new MockFor(UploadBuildArtifactsTask)
        taskMock.demand.execute { }
        taskMock.use {
            project.tasks.getByName('uploadBuildArtifacts').execute()
        }
    }

    @Test
    void shouldExecuteDownloadArtifactsTask(){
        def taskMock = new MockFor(DownloadArtifactsTask)
        taskMock.demand.execute { }
        taskMock.use {
            project.tasks.getByName('downloadArtifacts').execute()
        }
    }

    def taskShouldDependOn(task, requiredDependency) {
        for (def dependency : project.tasks.findByName(task).dependsOn) {
            if (dependency == requiredDependency) {
                return
            } else if (dependency instanceof List) {
                for (def d : dependency) {
                    if (d == requiredDependency) {
                        return
                    }
                }
            }
        }
        fail("Task ${task} does not declare a dependency on ${requiredDependency}")
    }

    def taskShouldExist(task) {
        assertThat("Task '${task}' does not exist on project", project.tasks.findByName(task), notNullValue())
    }
}
