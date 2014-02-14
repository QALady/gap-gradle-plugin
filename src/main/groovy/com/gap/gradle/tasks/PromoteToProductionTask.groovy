package com.gap.gradle.tasks

import groovy.io.*

import org.gradle.api.Project

import com.gap.gradle.jenkins.JenkinsClient
import com.gap.gradle.jenkins.JenkinsRunner
import com.gap.gradle.tasks.annotations.Require
import com.gap.gradle.tasks.annotations.RequiredParameters

@RequiredParameters([
	@Require(parameter = 'prodDeploy.sha1IdList', description = "SHA1 ID List of the chef objects to be promoted to production.")
])

class PromoteToProductionTask extends WatchmenTask {
	private Project project

    JenkinsClient jClient
    JenkinsRunner jRunner

    PromoteToProductionTask(Project project) {
		super(project)
		this.project = project
	}

	void execute() {
        //validate required configurations
        validate()
		init()
		// promoteCookbookVersion to Prod Server.
		publishCookbookToProdChefServer()
		// promoteChefObjectsToprodServer. (involves looping thru given sha1 ids
		promoteChefObjectsToProdServer()
	}
	
	def init() {
		def jConfig = this.project.jenkins
		jClient = new JenkinsClient(jConfig.knifeServerUrl, jConfig.knifeUser, jConfig.knifeAuthToken)
		jRunner = new JenkinsRunner(jClient)
	}

	def promoteChefObjectsToProdServer() {
        for (sha1Id in project.prodDeploy.sha1IdList) {
            def jobParams = [:]
            jobParams.put("COMMIT_ID", sha1Id)
            jobParams.put("TAG_MESSAGE", "Tag Message") //TODO: should be comment from EC procedure + ServiceNow ticket number
            jRunner.runJob(project.jenkins.knifeJobName, jobParams)
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
