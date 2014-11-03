package com.gap.gradle.tasks

import static com.gap.gradle.extensions.GapWMSegmentDsl.*

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.gradle.extensions.GapWMSegmentDslAction
import com.gap.gradle.utils.ShellCommandException
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
		segmentPhases.each { phase ->
			createPhaseProcedure(phase)
		}
	}

	def createPhaseProcedure(phase) {
		def procedureName = "perform_${phase}_actions_" + commanderClient.getJobId()
		def createProcConfig = [description:'dynamic procedure created by gradle task gap_wm_segmentdsl']
		commanderClient.createProcedure(projectName, procedureName, createProcConfig)
		def phasePropertyName = phase.toString().replaceAll("_", "")
		commanderClient.setECProperty("/myJob/watchmen_config/${phasePropertyName}StepProcedureName", procedureName)
		logger.info("segmentdsl phase actions: " + segmentDsl[phase])
		segmentDsl[phase].each {dslAction ->
			logger.info("phase dsl action: " + dslAction)
			createPhaseECStep(procedureName, dslAction)
		}
	}

	def createPhaseECStep(procedureName, o) {
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
		def subProjectName = checkPromotedPlugin(subProject)
		logger.info("Creating Step in ($projectName:$procedureName). stepName: '$stepName' (Delegating to: ${subProjectName}:${subProcedure})")

		def ecStepConfig = [:]
		ecStepConfig.put('condition', dsl.getECStepRunCondition())
		ecStepConfig.put('subproject', subProjectName)
		ecStepConfig.put('subprocedure', subProcedure)
		ecStepConfig.put('parallel', dsl.getECParallelStep())
		ecStepConfig.put('actualParameter', dsl.getECParameters())
		logger.info("Step Config: " + ecStepConfig.toString())
		commanderClient.createStep(projectName, procedureName, stepName, ecStepConfig)
	}

	def checkPromotedPlugin(givenPlugin) {
		def promotedPlugin = givenPlugin
		try {
			def pluginsXml = commanderClient.getPlugin(givenPlugin)
			logger.debug("plugins slurpedXml:" + pluginsXml)
			pluginsXml.each { plugin ->
				if (plugin.promoted == '1') {
					promotedPlugin = plugin.pluginName
				}
			}
			logger.info("promoted Plugin and Version of given $givenPlugin is: $promotedPlugin")
		} catch (all) {}

		return promotedPlugin
	}
}