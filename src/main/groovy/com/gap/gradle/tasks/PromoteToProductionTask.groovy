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
	@Require(parameter = 'ecJobId', description = "EC JobId to put in the jenkins job comment."),
	@Require(parameter = 'ecUser', description = "EC User that triggered this job to put in the jenkins job comment."),
	@Require(parameter = 'tagMessageComment', description = "Comment for this deploy."),
	@Require(parameter = 'ticketId', description = "Approved Service Center Ticket ID."),
    @Require(parameter = 'jenkins.knifeServerUrl', description = "Jenkins Server URL to trigger job to promote chef objects to prod."),
    @Require(parameter = 'jenkins.knifeUser', description = "Jenkins User ID to trigger job"),
    @Require(parameter = 'jenkins.knifeAuthToken', description = "Jenkins API Auth token to trigger job."),
    @Require(parameter = 'jenkins.knifeJobName', description = "Jenkins Job name to trigger.")
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
        super.validate()
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
		"${project.ticketId}-[ec-user:${project.ecUser},ec-jobid:${project.ecJobId}] ${project.tagMessageComment}" 
	}
}
