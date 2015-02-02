package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.pipeline.ec.CommanderClient;
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

@RequiredParameters([
	@Require(parameter = 'segment', description = 'the WM Segment DSL')
])
class ConfigureDefaultResourceFromDsl extends WatchmenTask {
	def logger = LogFactory.getLog(ConfigureDefaultResourceFromDsl)
	private Project project
	private CommanderClient commanderClient
	def segmentDsl
	
	ConfigureDefaultResourceFromDsl(Project project, commanderClient = new CommanderClient()) {
		super(project)
		this.project = project
		this.commanderClient = commanderClient
		this.segmentDsl = project.segment
	}

	
	def execute() {
		def defaultResourceValue=segmentDsl.resourceName ?: 'default' 
		def defaultResourcePath="/myJob/watchmen_config/config/defaultResource"
		logger.info("configuring default resource: " + defaultResourcePath)
		commanderClient.setECProperty(defaultResourcePath, defaultResourceValue)
	}
	
}
