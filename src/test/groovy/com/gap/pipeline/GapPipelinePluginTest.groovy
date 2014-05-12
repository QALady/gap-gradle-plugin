package com.gap.pipeline

import static helpers.Assert.*
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

import com.gap.gradle.tasks.GetResolvedVersionTask
import com.gap.gradle.tasks.PromoteArtifactsTask
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
        taskShouldExist('ivyIdentifiers', project)
    }

    @Test
    void ivyDependenciesTaskShouldBeAddedToProject() {
        taskShouldExist('ivyDependencies', project)
    }

    @Test
    void unzipIntegrationTestsTaskShouldBeAddedToProject() {
        taskShouldExist('unzipIntegrationTests', project)
    }

    @Test
    void prepareForProductionDeployTaskIsAddedToTheProject() {
        taskShouldExist('prepareForProductionDeploy', project)
    }

    @Test
    void getResolvedVersionTaskIsAddedToTheProject() {
        taskShouldExist('getResolvedVersion', project)
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
        shouldExecuteTask(project, 'prepareForProductionDeploy', PrepareForProductionDeployTask)
    }

    @Test
    void validatePrepareForProductionInputTaskIsAddedToTheProject() {
        taskShouldExist('validatePrepareForProductionInput', project)
    }

    @Test
    void validateGenerateChangeListReportTaskIsAddedToTheProject() {
        taskShouldExist('generateChangeListReport', project)
    }

    @Test
    void verifyPrepareForProductionTaskDependencies (){
        taskShouldDependOn('prepareForProductionDeploy', 'validatePrepareForProductionInput')
        taskShouldDependOn('prepareForProductionDeploy', 'generateChangeListReport')
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
    void shouldExecuteGenerateChangeListReportTask() {
        def mockTask = new MockFor(GenerateChangeListReportTask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('generateChangeListReport')
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
        taskShouldExist('setupBuildDirectories', project)
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
        taskShouldExist('uploadBuildArtifacts', project)
    }


    @Test
    void downloadArtifactsTaskIsAddedToTheProject(){
        taskShouldExist('downloadArtifacts', project)
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

    @Test
    void shouldAddPromoteArtifactsTaskToProject(){
        taskShouldExist("promoteArtifacts", project)
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

    @Test
    void shouldAddPopulateSegmentRegistryTask(){
        taskShouldExist("populateSegmentRegistry", project)
    }

    @Test
    void shouldExecutePromoteArtifactsTask() {
        shouldExecuteTask(project,'promoteArtifacts', PromoteArtifactsTask)
    }

    @Test
    void shouldExecuteGetResolvedVersionTask() {
        shouldExecuteTask(project,'getResolvedVersion', GetResolvedVersionTask)
    }

    @Test
    void shouldNotExecutePopulateSegmentRegistryIfProjectIsNotRoot(){
        def parent = ProjectBuilder.builder().withName('parent')build()
        new File("${parent.projectDir.path}/childDir/dir").mkdirs()
        def childProjectDir =  new File("${parent.projectDir.path}/childDir")


        def childProject = ProjectBuilder.builder().withName('child').withParent(project).withProjectDir(childProjectDir).build()
        childProject.apply plugin: 'gappipeline'

        shouldNotExecuteTask(childProject, 'populateSegmentRegistry', PopulateSegmentRegistryTask)
    }

}
