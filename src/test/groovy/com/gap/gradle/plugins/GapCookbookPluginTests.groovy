package com.gap.gradle.plugins
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.matchers.JUnitMatchers.containsString
import static org.junit.rules.ExpectedException.none

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class GapCookbookPluginTests {
    private Project project
    @Rule
    public final ExpectedException exception = none()
    private Task publishCookbookTask

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        publishCookbookTask = project.tasks.findByName('publishCookbookToChefServer2')
    }


    @Test
    void publishCookbookToChefServer_taskIsAddedToProject () {
        def task = project.tasks.findByName('publishCookbookToChefServer2')
        assertEquals('publishCookbookToChefServer2', task.name)
    }

    @Test
    void publishCookbookToChefServer_shouldThrowException_whenJenkinsServerUrlIsNotConfigured(){
        assertThrowsExceptionWithMessage("No jenkins url configured", {publishCookbookTask.execute()})
    }

    @Test
    void publishCookbookToChefServer_shouldThrowException_whenJenkinsUserNameIsNotConfigured(){
        project.jenkins.serverUrl = "testserver"
        assertThrowsExceptionWithMessage("No jenkins user configured", {publishCookbookTask.execute()})
    }

    @Test
    void publishCookbookToChefServer_shouldThrowException_whenJenkinsApiTokenIsNotConfigured(){
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
