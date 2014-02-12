package com.gap.gradle.tasks

import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static junit.framework.Assert.assertFalse
import static org.junit.internal.matchers.StringContains.containsString

import com.gap.gradle.jenkins.JenkinsClient
import com.gap.gradle.jenkins.JenkinsRunner

@Ignore
class TriggerProdDeployTaskTest{

    private Task triggerProdDeployTask
    private Project project
    def mockJenkinsRunner

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapproddeploy'
        triggerProdDeployTask = project.tasks.findByName('triggerProdDeployTask')

        mockJenkinsRunner = new MockFor(JenkinsRunner)
    }

    @Test
    void shouldThrowException_whenJenkinsServerUrlIsNotConfigured(){
        assertThrowsExceptionWithMessage("No jenkins url configured", {triggerProdDeployTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsUserNameIsNotConfigured(){
        project.jenkins.knifeServerUrl = "testserver"
        assertThrowsExceptionWithMessage("No jenkins user configured", {triggerProdDeployTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsApiTokenIsNotConfigured(){
        project.jenkins.knifeServerUrl = "jenkins"
        project.jenkins.knifeUser = "jenkins_user"
        assertThrowsExceptionWithMessage("No jenkins auth-token configured", {triggerProdDeployTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsJobNameIsNotConfigured(){
        project.jenkins.knifeServerUrl = "jenkins"
        project.jenkins.knifeUser = "jenkins_user"
        project.jenkins.knifeAuthToken = "jenkins_auth"
        assertThrowsExceptionWithMessage("No jenkins jobName configured", {triggerProdDeployTask.execute()})
    }

//    @Test
    void shouldTriggerPromoteChefObjectsJob_whenAllParametersArePassedr(){
//        setupTaskProperties()
//        mockCookbookUploader.demand.upload { cookbook, env ->
//            assertEquals("myapp", cookbook)
//            assertEquals("local", env)
//        }
//        mockCookbookUtil.demand.doesCookbookExist { return false }
//
//        mockCookbookUtil.use {
//           mockCookbookUploader.use {
//               promoteChefObjectsTask.execute()
//           }
//        }
    }

//    @Test
    void shouldGetCookbookNameFromMetadata_whenCookbookNameIsNotProvided(){
//        setupTaskProperties()
//
//        mockCookbookUtil.demand.doesCookbookExist {return false}
//        mockCookbookUploader.demand.upload { cookbook, env ->
//            assertEquals("myapp", cookbook)
//            assertEquals("local", env)
//        }
//
//        mockCookbookUtil.use {
//            mockCookbookUploader.use {
//                publishCookbookTask.execute()
//            }
//        }
    }

//    @Test
    void shouldNotTriggerUpload_whenCookbookVersionAlreadyExistsInChef(){
//        setupTaskProperties()
//
//        mockCookbookUtil.demand.doesCookbookExist { return true }
//
//        mockCookbookUploader.ignore.upload {}
//
//        mockCookbookUploader.use{
//            mockCookbookUtil.use{
//                publishCookbookTask.execute()
//            }
//        }
    }

    private void setupTaskProperties() {
        project.jenkins.knifeServerUrl = "jenkins"
        project.jenkins.knifeUser = "jenkins_user"
        project.jenkins.knifeAuthToken = "jenkins_password"
        project.chef.environment = "local"
    }

    void assertThrowsExceptionWithMessage(expectedMessage, Closure closure){
        try{
            closure()
            assertFalse("Expected exception with message '${expectedMessage} but got none", true)
        }
        catch(Exception ex){
            assertThat(ex.dump(), containsString(expectedMessage))
        }
    }
}