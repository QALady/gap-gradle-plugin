package com.gap.gradle.plugins

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.matchers.JUnitMatchers.containsString

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
