package com.gap.gradle.tasks

import static com.gap.gradle.extensions.GapWMSegmentDsl.*

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.gradle.extensions.GapWMSegmentDslAction
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters


@RequiredParameters([
		@Require(parameter = 'segment', description = 'the WM Segment DSL')
])
class CreateECProcedureTask extends WatchmenTask {
	def logger = LogFactory.getLog(CreateECProcedureTask)
	private Project project
	private CommanderClient commanderClient
	def plugins =[:]
	def segmentDsl
	def projectName = "WM Temporary Procedures"

	CreateECProcedureTask(Project project, commanderClient = new CommanderClient()) {
		super(project)
		this.project = project
		this.commanderClient = commanderClient
		this.segmentDsl = project.segment
	}

	def execute() {
		//plugins = getPromotedPlugins()
		segmentPhases.each { phase ->
			createPhaseProcedure(phase)
		}
	}

	def createPhaseProcedure(phase) {
		def procedureName = "perform_${phase}_actions_" + commanderClient.getJobId()
		def createProcConfig = [description:'dynamic procedure created by gradle task gap_wm_segmentdsl']
		commanderClient.createProcedure(projectName, procedureName, createProcConfig)
		commanderClient.setECProperty("/myJob/watchmen_config/${phase}StepProcedureName", procedureName)
		segmentDsl[phase].each {dslAction ->
			runPhaseECStep(procedureName, dslAction)
		}
	}

	def runPhaseECStep(procedureName, o) {
		def subProject, subProcedure
		assert o instanceof GapWMSegmentDslAction
		GapWMSegmentDslAction dsl = (GapWMSegmentDslAction) o
		logger.info("processing segment step:" + dsl.toString())
		def stepName = "Perform ${dsl.name}: ${dsl.action}"
		if (dsl.hasSubProject()) {
			(subProject, subProcedure) = dsl.action.split(":")
		} else {
			(subProject, subProcedure) = [projectName, dsl.action.toString()]
		}
		def subProjectName = plugins.get(subProject) ?: subProject
		logger.info("Creating Step in ($projectName:$procedureName). stepName: '$stepName' (Delegating to: ${subProjectName}:${subProcedure})")

		def ecStepConfig = [:]
		
		commanderClient.createStep(projectName, procedureName, stepName, ecStepConfig)
	}

	def getPromotedPlugins() {
		def pluginsJson = commanderClient.getPlugins()
		logger.debug("plugins slurpedJson:" + pluginsJson)
		def returnHash = [:]
		pluginsJson.each { plugin ->
			logger.debug("plugin: ${plugin.pluginName} ${plugin.promoted == '1' ? 'promoted' : 'not promoted'}")
			if (plugin.promoted == '1') {
				returnHash.put(plugin.pluginKey, plugin.pluginName)
			}
		}
		logger.debug("getPlugins promoted Plugins and Versions : $returnHash")
		return returnHash
	}
}
