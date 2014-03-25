package com.gap.gradle.tasks

import com.gap.gradle.jenkins.JenkinsClient
import com.gap.gradle.jenkins.JenkinsRunner
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.gradle.api.Project

@RequiredParameters([
	@Require(parameter = 'prodDeploy.cookbook.name', description = "cookbook name"),
	@Require(parameter = 'jenkins.cookbookServerUrl', description = "Jenkins Server URL to trigger job to promote chef objects to prod."),
	@Require(parameter = 'jenkins.cookbookUser', description = "Jenkins User ID to trigger job"),
	@Require(parameter = 'cookbookJenkinsApiAuthToken', description = "Jenkins API Auth token passed from the EC job."),
	@Require(parameter = 'chef.environment', description = "used to define the Jenkins job name for the cookbook")
])
class PromoteCookbookToProductionTask extends WatchmenTask {
	private Project project

	JenkinsClient jClient
	JenkinsRunner jRunner

	PromoteCookbookToProductionTask(Project project) {
		super(project)
		this.project = project
	}

	void execute() {
		super.validate()
		init()
		jRunner.runJob("cookbook-${project.prodDeploy.cookbook.name}-${project.chef.environment}")
	}
	
	def init() {
		def jConfig = this.project.jenkins
		jClient = new JenkinsClient(jConfig.cookbookServerUrl, jConfig.cookbookUser, project.cookbookJenkinsApiAuthToken)
		jRunner = new JenkinsRunner(jClient, 15000, 9000000)
	}

}
