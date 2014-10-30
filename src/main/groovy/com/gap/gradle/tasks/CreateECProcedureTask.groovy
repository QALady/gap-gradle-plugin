package com.gap.gradle.tasks

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require;
import com.gap.pipeline.tasks.annotations.RequiredParameters;

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project


@RequiredParameters([
		@Require(parameter = 'segment', description = 'the WM Segment DSL')
])
class CreateECProcedureTask extends WatchmenTask {
	def logger = LogFactory.getLog(CreateECProcedureTask)
	private Project project
	private CommanderClient commanderClient
	def phaseList = ['prepare', 'test', 'approve', 'finally']


	CreateECProcedureTask(Project project, commanderClient = new CommanderClient()) {
		super(project)
		this.project = project
		this.commanderClient = commanderClient
	}

	def execute() {
		def plugins = getPromotedPlugins()
	}

	def getPromotedPlugins() {
		def pluginsJson = commanderClient.getPlugins()
		logger.info("plugins slurpedJson:" + pluginsJson)
		def returnHash = [:]
		pluginsJson.each { plugin ->
			logger.debug("plugin: ${plugin.pluginName} ${plugin.promoted == '1' ? 'promoted' : 'not promoted'}")
			if (plugin.promoted == '1') {
				returnHash.put(plugin.pluginKey, plugin.pluginName)
			}
		}
		logger.info("getPlugins promoted Plugins and Versions : $returnHash")
		return returnHash
	}
}
