package com.gap.gradle.plugins.chef

import groovy.json.JsonBuilder

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.gap.gradle.ProdDeployConfig

class PromoteToProductionTaskIntegrationTest {

	Project project

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Test
	void shouldPromoteChefObjectsToServerUsingJenkinsPipeline() {
		project = ProjectBuilder.builder().build()
		createProdDeployConfigFile()
		project.apply plugin: 'gapproddeploy'
		project.prodDeploy.sha1IdList = ["f06cfb4867a8aafd1fb5c6a01add274ba22f6ddc", "2c8518f1d8b11caaa52fee996f1cb3f1eeb5fc04"]
		project.jenkins.knifeServerUrl = "http://jenkins01.phx.gapinc.dev:8080"
		project.jenkins.knifeUser = "kr8s8k9"
		project.jenkins.knifeJobName = "TagProdReady"
		project.jenkins.knifeAuthToken = "4661bb66b1f850bdff9c3ce5f5daca65"

		def triggerProdDeployTask = project.tasks.findByName('promoteToProduction')

		triggerProdDeployTask.execute()
	}

	void createProdDeployConfigFile() {
		ProdDeployConfig config = new ProdDeployConfig()
		config.sha1IdList = ["1234", "4567"]

		def jsonBuilder = new JsonBuilder(config)
		def fileWriter = new FileWriter("${ProdDeployConfig.PARAMJSON}")
		jsonBuilder.writeTo(fileWriter)
		fileWriter.close()
	}
}
