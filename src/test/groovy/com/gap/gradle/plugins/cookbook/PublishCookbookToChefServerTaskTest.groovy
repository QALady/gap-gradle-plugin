package com.gap.gradle.plugins.cookbook
import static junit.framework.Assert.assertFalse
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.internal.matchers.StringContains.containsString

import com.gap.gradle.chef.CookbookUploader
import com.gap.gradle.chef.CookbookUtil
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class PublishCookbookToChefServerTaskTest {

    Project project
    Task publishCookbookTask
    def mockCookbookUploader
    def mockCookbookUtil

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        publishCookbookTask = project.tasks.findByName('publishCookbookToChefServer')
        mockCookbookUploader = new MockFor(CookbookUploader)
        mockCookbookUtil = new MockFor(CookbookUtil)
        project.chef.metadata = [ name: "myapp", version: "1.1.13" ]
    }

    @Test
    void shouldThrowException_whenJenkinsServerUrlIsNotConfigured(){
        assertThrowsExceptionWithMessage("No jenkins url configured", {publishCookbookTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsUserNameIsNotConfigured(){
        project.jenkins.cookbookServerUrl = "testserver"
        assertThrowsExceptionWithMessage("No jenkins user configured", {publishCookbookTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsApiTokenIsNotConfigured(){
        project.jenkins.cookbookServerUrl = "jenkins"
        project.jenkins.cookbookUser = "jenkins_user"
        assertThrowsExceptionWithMessage("No jenkins auth-token configured", {publishCookbookTask.execute()})
    }

    @Test
    void shouldTriggerUploadOfCookbookUsingJenkinsPipeline_whenTheCookbookDoesNotExistInChefServer(){
        setupTaskProperties()
        mockCookbookUploader.demand.upload { cookbook, env ->
            assertEquals("myapp", cookbook)
            assertEquals("local", env)
        }
        mockCookbookUtil.demand.doesCookbookExist { return false }

        mockCookbookUtil.use {
           mockCookbookUploader.use {
               publishCookbookTask.execute()
           }
        }
    }

    @Test
    void shouldGetCookbookNameFromMetadata_whenCookbookNameIsNotProvided(){
        setupTaskProperties()

        mockCookbookUtil.demand.doesCookbookExist {return false}
        mockCookbookUploader.demand.upload { cookbook, env ->
            assertEquals("myapp", cookbook)
            assertEquals("local", env)
        }

        mockCookbookUtil.use {
            mockCookbookUploader.use {
                publishCookbookTask.execute()
            }
        }
    }

    @Test
    void shouldNotTriggerUpload_whenCookbookVersionAlreadyExistsInChef(){
        setupTaskProperties()

        mockCookbookUtil.demand.doesCookbookExist { return true }

        mockCookbookUploader.ignore.upload {}

        mockCookbookUploader.use{
            mockCookbookUtil.use{
                publishCookbookTask.execute()
            }
        }
    }

    private void setupTaskProperties() {
        project.jenkins.cookbookServerUrl = "jenkins"
        project.jenkins.cookbookUser = "jenkins_user"
        project.jenkins.cookbookAuthToken = "jenkins_password"
        project.chef.environment = "local"
        project.chef.cookbookName = "myapp"

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
