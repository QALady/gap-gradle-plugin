package com.gap.gradle.plugins.chef

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class PromoteToProductionTaskIntegrationTest {

	Project project

	@Test
	void shouldPromoteChefObjectsToServerUsingJenkinsPipeline() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: 'gapproddeploy'
		project.prodDeploy.sha1IdList = ["f06cfb4867a8aafd1fb5c6a01add274ba22f6ddc", "2c8518f1d8b11caaa52fee996f1cb3f1eeb5fc04"]
		project.prodDeploy.ecUser = "integration-test"
		project.prodDeploy.ecJobId = "9999"
		project.prodDeploy.ticketId = "T12345"
		project.prodDeploy.comment = "this is the comment integration test is triggering with."
		project.jenkins.knifeServerUrl = "http://jenkins01.phx.gapinc.dev:8080"
		project.jenkins.knifeUser = "kr8s8k9"
		project.jenkins.knifeJobName = "TagProdReady"
		project.jenkins.knifeAuthToken = "4661bb66b1f850bdff9c3ce5f5daca65"

		def triggerProdDeployTask = project.tasks.findByName('promoteToProduction')

		triggerProdDeployTask.execute()
	}
}
