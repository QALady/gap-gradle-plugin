package com.gap.gradle.tasks

import org.gradle.api.Project

import com.gap.gradle.ProdDeployConfig
import com.gap.gradle.jenkins.JenkinsClient
import com.gap.gradle.jenkins.JenkinsRunner
import groovy.json.JsonSlurper
import groovy.io.*

class PromoteToProductionTask extends WatchmenTask {
	private Project project
	private def deployConfig

	PromoteToProductionTask(Project project) {
		super(project)
		this.project = project
	}

	void execute() {
        //validate required configurations
        requireJenkinsConfig()
		// read the json file from the prodDeployParameterJsonPath param into ProdDeployConfig
		loadConfigFromJson()
		// promoteChefObjectsToprodServer. (involves looping thru given sha1 ids
		promoteChefObjectsToProdServer()
		// promoteCookbookVersion to Prod Server.
		publishCookbookToProdChefServer()
		// call the task that runs chef-client on all the prod nodes. 
	}
	
	def loadConfigFromJson() {
        File configFile = new File(ProdDeployConfig.PARAMJSON)
        if(!configFile.exists()){
            throw new Exception("Prod Deploy Config file (${ProdDeployConfig.PARAMJSON}) is missing")
        }
        deployConfig = new JsonSlurper().parseText(configFile.text)
        deployConfig.each { println it }
	}
	
	def promoteChefObjectsToProdServer() {
        for (sha1Id in deployConfig.sha1IdList) {
            def jobParams = [:]
            jobParams.put("COMMIT_ID", sha1Id)
            jobParams.put("TAG_MESSAGE", "Tag Message")
            promoteChefObjectsByCallingJenkinsJob(jobParams)
        }
	}

	def publishCookbookToProdChefServer() {
		
	}

    def promoteChefObjectsByCallingJenkinsJob(jobParams) {
        def jConfig = project.jenkins
        JenkinsClient jClient = new JenkinsClient(jConfig.knifeServerUrl, jConfig.knifeUser, jConfig.knifeAuthToken)
        JenkinsRunner jRunner = new JenkinsRunner(jClient)
        jRunner.runJob(jConfig.knifeJobName, jobParams)
    }

    def requireJenkinsConfig() {
        if (!project.jenkins.knifeServerUrl) {
            throw new Exception("No jenkins url configured")
        } else if (!project.jenkins.knifeUser) {
            throw new Exception("No jenkins user configured")
        } else if (!project.jenkins.knifeAuthToken) {
            throw new Exception("No jenkins auth-token configured")
        } else if (!project.jenkins.knifeJobName) {
            throw new Exception("No jenkins jobName configured")
        }
    }
}
