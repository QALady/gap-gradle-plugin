package com.gap.gradle.plugins.cookbook
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.Assert.assertFalse
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

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        publishCookbookTask = project.tasks.findByName('publishCookbookToChefServer2')
    }

    @Test
    void shouldThrowException_whenJenkinsServerUrlIsNotConfigured(){
        assertThrowsExceptionWithMessage("No jenkins url configured", {publishCookbookTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsUserNameIsNotConfigured(){
        project.jenkins.serverUrl = "testserver"
        assertThrowsExceptionWithMessage("No jenkins user configured", {publishCookbookTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsApiTokenIsNotConfigured(){
        project.jenkins.serverUrl = "jenkins"
        project.jenkins.user = "jenkins_user"
        assertThrowsExceptionWithMessage("No jenkins auth-token configured", {publishCookbookTask.execute()})
    }

    @Test
    void shouldSuccessfullyUploadCookbook(){
        project.jenkins.serverUrl = "jenkins"
        project.jenkins.user = "jenkins_user"
        project.jenkins.authToken = "jenkins_password"
        project.chef.environment = "local"
        project.chef.cookbookName = "myapp"

        def mockCookbookUploader = new MockFor(CookbookUploader)
        mockCookbookUploader.demand.upload { cookbook, env ->
            assertEquals("myapp", cookbook)
            assertEquals("local", env)
        }

        mockCookbookUploader.use {
            publishCookbookTask.execute()
        }
    }

    @Test
    void shouldGetCookbookNameFromMetadata_whenCookbookNameIsNotProvided(){
        project.jenkins.serverUrl = "jenkins"
        project.jenkins.user = "jenkins_user"
        project.jenkins.authToken = "jenkins_password"
        project.chef.environment = "local"

        def mockCookbookUtil = new MockFor(CookbookUtil)
        mockCookbookUtil.demand.metadataFrom { path ->
            [ name: "myapp", version: "1.1.13" ]
        }

        def mockCookbookUploader = new MockFor(CookbookUploader)
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
