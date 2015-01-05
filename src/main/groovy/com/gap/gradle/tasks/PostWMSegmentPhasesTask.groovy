package com.gap.gradle.tasks

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

@RequiredParameters([
		@Require(parameter = 'segment', description = 'the WM Segment DSL')
])
class PostWMSegmentPhasesTask extends WatchmenTask{
	def logger = LogFactory.getLog(PostWMSegmentPhasesTask)
	Project project
	CommanderClient commanderClient
	def segmentDsl

	PostWMSegmentPhasesTask(Project project, commanderClient = new CommanderClient()) {
		super(project)
		this.project=project
		this.commanderClient=commanderClient
		this.segmentDsl = project.segment
	}

	def execute() {
		executeCreateJobLinks()
		executeDeleteDynamicNodes()		
	}

	def executeCreateJobLinks() {
		segmentDsl.jobLinks.each { jobLink ->
			logger.info("Creating EC JobLink urlLabel: ${jobLink.name}, urlLink: ${jobLink.link}")
			commanderClient.setECProperty("/myJob/report-urls/${jobLink.name}","${jobLink.link}")
		}
	}

	def executeDeleteDynamicNodes() {
		def easyCreateParams
		def projectName = "Nova-CLI"
		def procedureName = "Easy Delete"
		segmentDsl.dynamicNodes.each { node ->
			easyCreateParams = [:]
			easyCreateParams.put("resourceToDelete", "${node.name}".toString())
			easyCreateParams.put("tenant", "${node.openstackTenant}".toString())
			commanderClient.runProcedure(projectName, procedureName, easyCreateParams)
			logger.info("Deleted Dynamic node:  ${node.name} on ${node.openstackTenant} tenant.")
		}
	}

}
