package com.gap.gradle.tasks

import groovy.io.*

import org.gradle.api.Project

import com.gap.gradle.jenkins.JenkinsClient
import com.gap.gradle.jenkins.JenkinsRunner
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

@RequiredParameters([
	@Require(parameter = 'prodDeploy.sha1IdList', description = "SHA1 ID List of the chef objects to be promoted to production."),
	@Require(parameter = 'prodDeploy.ecJobId', description = "EC JobId to put in the jenkins job comment."),
	@Require(parameter = 'prodDeploy.ecUser', description = "EC User that triggered this job to put in the jenkins job comment."),
	@Require(parameter = 'prodDeploy.comment', description = "Comment for this deploy."),
	@Require(parameter = 'prodDeploy.ticketId', description = "Approved Service Center Ticket ID.")
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
            jobParams.put("TAG_MESSAGE", getTagMessage()) //TODO: should be comment from EC procedure + ServiceNow ticket number
            jRunner.runJob(project.jenkins.knifeJobName, jobParams)
        }
	}

	def publishCookbookToProdChefServer() {
		// call the gap cookbook plugin
	}

	String getTagMessage() {
		def p = project.prodDeploy
		"${p.ticketId}-[ec-user:${p.ecUser},ec-jobid:${p.ecJobId}] ${p.comment}" 
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
