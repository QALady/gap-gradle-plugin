package com.gap.gradle.plugins.chef
import com.gap.pipeline.ec.CommanderClient
import com.gap.gradle.plugins.helpers.Util
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PromoteToProductionTaskIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    Project project

	@Test
	void shouldPromoteChefObjectsToServerUsingJenkinsPipeline() {
	project = ProjectBuilder.builder().build()
		project.metaClass.ecUser = "integration-test"
		project.metaClass.paramJsonPath = "src/test/groovy/com/gap/gradle/resources/"
		project.metaClass.ecJobId = "9999"
		project.metaClass.ticketId = "T12345"
		project.metaClass.tagMessageComment = "this is the comment integration test is triggering with."
		project.apply plugin: 'gapproddeploy'
		project.prodDeploy.sha1IdList = ["f06cfb4867a8aafd1fb5c6a01add274ba22f6ddc", "2c8518f1d8b11caaa52fee996f1cb3f1eeb5fc04"]
		project.jenkins.knifeServerUrl = "http://dgphxaciap004.phx.gapinc.dev:8080/"
		project.jenkins.knifeUser = "integtest"
		project.jenkins.knifeJobName = "TagProdReady"
		project.jenkins.knifeAuthToken = "15497c7c234c5940a3573672363e692c"

        if (Util.isRunningInPipeline()){
            executeTask(project)
        }else{
            executeTaskWithMockedCommanderClient( project)
        }
	}

    private executeTaskWithMockedCommanderClient(project) {
        def mockCommanderClient = new MockFor(CommanderClient)
        mockCommanderClient.ignore.with {
            getUserId {"integration-test-user"}
            getJobId {"nonexistent-job-id"}
        }
        mockCommanderClient.use {
            executeTask(project)
        }
    }

    private executeTask(Project project) {
        def triggerProdDeployTask = project.tasks.findByName('promoteChefObjectsToProduction')
        triggerProdDeployTask.execute()
    }
}
