package com.gap.gradle.plugins.openstack

import groovy.mock.interceptor.MockFor
import com.gap.gradle.jenkins.JenkinsRunner
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static helpers.Assert.assertThrowsExceptionWithMessage
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals

class CleanUpOrphanObjectsTaskTest {

    Project project
    Task cleanUpOrphanObjectsTask
    def mockJenkinsRunner

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'openstack-cleanup'
        cleanUpOrphanObjectsTask = project.tasks.findByName('cleanUpOrphanObjects')
        mockJenkinsRunner = new MockFor(JenkinsRunner)
    }

    @Test
    void shouldThrowException_whenJenkinsServerUrlIsNotConfigured(){
        assertThrowsExceptionWithMessage("Missing required parameter: 'jenkins.knifeServerUrl'", {cleanUpOrphanObjectsTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsUserNameIsNotConfigured(){
        project.jenkins.knifeServerUrl = "testserver"
        assertThrowsExceptionWithMessage("Missing required parameter: 'jenkins.knifeUser'", {cleanUpOrphanObjectsTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsApiTokenIsNotConfigured(){
        project.jenkins.knifeServerUrl = "testserver"
        project.jenkins.knifeUser = "jenkins_user"
        assertThrowsExceptionWithMessage("Missing required parameter: 'jenkins.knifeAuthToken'", {cleanUpOrphanObjectsTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsJobNameIsNotConfigured(){
        project.jenkins.knifeServerUrl = "testserver"
        project.jenkins.knifeUser = "jenkins_user"
        project.jenkins.knifeAuthToken = "token"
        assertThrowsExceptionWithMessage("Missing required parameter: 'jenkins.knifeCleanUpJobName'", {cleanUpOrphanObjectsTask.execute()})
    }

    @Test
    void shouldThrowException_whenNodeToDeleteIsNotConfigured(){
        project.jenkins.knifeServerUrl = "testserver"
        project.jenkins.knifeUser = "jenkins_user"
        project.jenkins.knifeAuthToken = "token"
        project.jenkins.knifeCleanUpJobName = "cleanupJob"
        assertThrowsExceptionWithMessage("Missing required parameter: 'nodeToDelete'", {cleanUpOrphanObjectsTask.execute()})
    }



    @Test
    void shouldInvokeConfiguredJenkinsCleanupJob(){
        setupTaskProperties()
        project.nodeToDelete = "someNode"
        mockJenkinsRunner.demand.runJob{jobName,jobParams ->
            assertEquals("cleanupJob", jobName)
            assertEquals("someNode", jobParams["NODE_NAME"])
        }
        mockJenkinsRunner.use{
            cleanUpOrphanObjectsTask.execute()
        }
    }

    private void setupTaskProperties() {
        project.jenkins.knifeServerUrl = "jenkins"
        project.jenkins.knifeUser = "user"
        project.jenkins.knifeAuthToken = "token"
        project.jenkins.knifeCleanUpJobName = "cleanupJob"
    }
}
