package com.gap.gradle.tasks

import com.gap.gradle.chef.CookbookUploader
import com.gap.gradle.chef.CookbookUtil

import static junit.framework.Assert.*
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.internal.matchers.StringContains.containsString
import static org.junit.rules.ExpectedException.none
import static org.mockito.Matchers.anyObject
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.*
import groovy.mock.interceptor.MockFor

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import com.gap.gradle.jenkins.JenkinsRunner
import com.gap.pipeline.ec.CommanderClient;

class PromoteToProductionTaskTest {

    @Rule
    public final ExpectedException exception = none()

    private Task promoteToProdTask
    private Task publishCookbookToChefServerTask
    private Project project
    def mockJenkinsRunner
	def mockCommanderClient
    def sha1IdList = ["1234", "5678"]
	def testuser = "testUser"
	def ecJobId = "9999"
	def ticketId = "T12131"
	def comment = "this is a comment for prod deploy"
    def mockCookbookUploader
    def mockCookbookUtil

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
		project.paramJsonPath = "src/test/groovy/com/gap/gradle/resources/"
		project.ticketId = ticketId
		project.tagMessageComment = comment
		project.apply plugin: 'gapproddeploy'
        project.apply plugin: 'gapcookbook'
		project.prodDeploy.sha1IdList = ["1234", "24343"]
        promoteToProdTask = project.tasks.findByName('promoteToProduction')
        publishCookbookToChefServerTask = project.tasks.findByName('publishCookbookToChefServer')
        mockJenkinsRunner = new MockFor(JenkinsRunner.class)
		mockCommanderClient = new MockFor(CommanderClient.class)
        mockCookbookUploader = new MockFor(CookbookUploader)
        mockCookbookUtil = new MockFor(CookbookUtil)
        project.chef.metadata = [ name: "myapp", version: "1.1.13" ]
    }

    @Test
    void shouldTriggerPromoteChefObjectsJob_whenAllParametersArePassed(){
        setupTaskProperties()

        mockCookbookUploader.demand.upload { cookbook, env ->
            assertEquals("myapp", cookbook)
            assertEquals("local", env)
        }
        mockCookbookUtil.demand.doesCookbookExist { return false }

        mockCookbookUtil.use {
            mockCookbookUploader.use {
                publishCookbookToChefServerTask.execute()
            }
        }

		def expectedTagMessage = ticketId + "-[ec-user:" + testuser + ",ec-jobid:" + ecJobId + "] " + comment 

        mockJenkinsRunner.demand.runJob (2) { jobName, params ->
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

    //@Test
    void shouldThrowException_whenJenkinsJobIsFailed(){
        setupTaskProperties()

        mockJenkinsRunner.demand.runJob(2) {jobName, params ->
            throw new Exception()
        }

        mockJenkinsRunner.use {
            promoteToProdTask.execute()
        }
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
        project.jenkins.knifeJobName = "jenkins_job"

        project.jenkins.cookbookServerUrl = "jenkins"
        project.jenkins.cookbookUser = "jenkins_user"
        project.jenkins.cookbookAuthToken = "jenkins_password"
        project.chef.environment = "local"
        project.chef.cookbookName = "myapp"
    }


}