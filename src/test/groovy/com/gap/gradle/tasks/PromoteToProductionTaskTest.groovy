package com.gap.gradle.tasks

import com.gap.gradle.jenkins.JenkinsRunner
import com.gap.pipeline.ec.CommanderClient
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertNotNull
import static org.junit.rules.ExpectedException.none

class PromoteToProductionTaskTest {

    @Rule
    public final ExpectedException exception = none()

    private Task promoteToProdTask
    private Project project
    def mockJenkinsRunner
	def mockCommanderClient
    def sha1IdList = ["1234", "5678"]
	def testuser = "testUser"
	def ecJobId = "9999"
	def ticketId = "T12131"
	def comment = "this is a comment for prod deploy"

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
		project.paramJsonPath = "src/test/groovy/com/gap/gradle/resources/"
		project.ticketId = ticketId
		project.tagMessageComment = comment
		project.apply plugin: 'gapproddeploy'
        project.apply plugin: 'gapcookbook'
		project.prodDeploy.sha1IdList = ["1234", "24343"]
        promoteToProdTask = project.tasks.findByName('promoteChefObjectsToProduction')
        mockJenkinsRunner = new MockFor(JenkinsRunner.class)
		mockCommanderClient = new MockFor(CommanderClient.class)
    }

    @Test
    void shouldTriggerPromoteChefObjectsJob_whenAllParametersArePassed(){
        setupTaskProperties()

		def expectedTagMessage = ticketId + "-[ec-user:" + testuser + ",ec-jobid:" + ecJobId + "] " + comment 

        mockJenkinsRunner.demand.runJob(2) { jobName, params ->
            assertEquals("jenkins_job", jobName)
			assertNotNull(params)
			assertEquals(expectedTagMessage, params.get("TAG_MESSAGE"))
        }

		mockCommanderClient.demand.getUserId(1) {testuser}
		mockCommanderClient.demand.getJobId(1) {ecJobId}
        mockJenkinsRunner.use {
			mockCommanderClient.use {
				promoteToProdTask.execute()
			}
        }
    }

    private void setupTaskProperties() {
        project.jenkins.knifeServerUrl = "jenkins"
        project.jenkins.knifeUser = "jenkins_user"
        project.chefJenkinsApiAuthToken = "jenkins_password"
        project.jenkins.knifeJobName = "jenkins_job"
    }
}