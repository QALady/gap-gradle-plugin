package com.gap.gradle.tasks

import groovy.io.*
import groovy.json.JsonSlurper

import org.gradle.api.Project

import com.gap.gradle.ProdDeployConfig
import com.gap.gradle.ProdDeployParameterConfig
import com.gap.gradle.jenkins.JenkinsClient
import com.gap.gradle.jenkins.JenkinsRunner
import com.gap.gradle.tasks.annotations.Require
import com.gap.gradle.tasks.annotations.RequiredParameters

@RequiredParameters([
	@Require(parameter = 'prodDeployParametersJsonAbsolutePath', description = "The absolute path of prodDeployParameters.json.")
])
class PromoteToProductionTask extends WatchmenTask {
	private Project project
	private ProdDeployParameterConfig deployConfig

    JenkinsClient jClient
    JenkinsRunner jRunner

    PromoteToProductionTask(Project project) {
		super(project)
		this.project = project
	}

	void execute() {
        //validate required configurations
        validate()
		// read the json file from the prodDeployParameterJsonPath param into ProdDeployConfig
		loadConfigFromJson()
		// promoteCookbookVersion to Prod Server.
		publishCookbookToProdChefServer()
		// promoteChefObjectsToprodServer. (involves looping thru given sha1 ids
		promoteChefObjectsToProdServer()
	}
	
	def loadConfigFromJson() {
        File configFile = new File("${project.prodDeployParametersJsonAbsolutePath}/${ProdDeployConfig.PARAMJSON}")
        if(!configFile.exists()){
            throw new Exception("Prod Deploy Config file (${ProdDeployConfig.PARAMJSON}) is missing")
        }
        deployConfig = new JsonSlurper().parseText(configFile.text)
        deployConfig.each { println it }
		def jConfig = this.project.prodJenkins
		jClient = new JenkinsClient(jConfig.knifeServerUrl, jConfig.knifeUser, jConfig.knifeAuthToken)
		jRunner = new JenkinsRunner(jClient)
	}

	def promoteChefObjectsToProdServer() {
        for (sha1Id in deployConfig.sha1IdList) {
            def jobParams = [:]
            jobParams.put("COMMIT_ID", sha1Id)
            jobParams.put("TAG_MESSAGE", "Tag Message")
            jRunner.runJob(project.prodJenkins.knifeJobName, jobParams)
        }
	}

	def publishCookbookToProdChefServer() {
		// call the gap cookbook plugin
	}

	def validate() {
		super.validate()
		requireJenkinsConfig()
	}

    def requireJenkinsConfig() {
        if (!project.prodJenkins.knifeServerUrl) {
            throw new Exception("No jenkins url configured")
        } else if (!project.prodJenkins.knifeUser) {
            throw new Exception("No jenkins user configured")
        } else if (!project.prodJenkins.knifeAuthToken) {
            throw new Exception("No jenkins auth-token configured")
        } else if (!project.prodJenkins.knifeJobName) {
            throw new Exception("No jenkins jobName configured")
        }
    }
}
