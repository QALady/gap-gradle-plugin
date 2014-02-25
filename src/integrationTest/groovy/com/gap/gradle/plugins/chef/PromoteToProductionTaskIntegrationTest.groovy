package com.gap.gradle.plugins.chef

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Ignore
import org.junit.Test


class PromoteToProductionTaskIntegrationTest {

	Project project

	@Test
    @Ignore
	void shouldPromoteChefObjectsToServerUsingJenkinsPipeline() {
		project = ProjectBuilder.builder().build()
		project.ecUser = "integration-test"
		project.paramJsonPath = "src/test/groovy/com/gap/gradle/resources/"
		project.ecJobId = "9999"
		project.ticketId = "T12345"
		project.tagMessageComment = "this is the comment integration test is triggering with."
		project.apply plugin: 'gapproddeploy'
		project.prodDeploy.sha1IdList = ["f06cfb4867a8aafd1fb5c6a01add274ba22f6ddc", "2c8518f1d8b11caaa52fee996f1cb3f1eeb5fc04"]
		project.jenkins.knifeServerUrl = "http://jenkins01.phx.gapinc.dev:8080"
		project.jenkins.knifeUser = "kr8s8k9"
		project.jenkins.knifeJobName = "TagProdReady"
		project.jenkins.knifeAuthToken = "4661bb66b1f850bdff9c3ce5f5daca65"

        def triggerProdDeployTask = project.apply plugin: 'gapcookbook'

        project.jenkins.cookbookServerUrl = "http://chefci.phx.gapinc.dev:8080"
        project.jenkins.cookbookUser = "em4l5d0"
        project.jenkins.cookbookAuthToken = "4661bb66b1f850bdff9c3ce5f5daca65"
        project.chef.cookbookName = "ref-app"

        triggerProdDeployTask.execute()
	}
}
