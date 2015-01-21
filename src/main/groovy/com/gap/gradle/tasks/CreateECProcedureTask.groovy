package com.gap.gradle.tasks

import static com.gap.gradle.extensions.GapWMSegmentDsl.segmentPhases

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.gradle.exceptions.DynamicNodesException
import com.gap.gradle.exceptions.WMSegmentDslLockResourceOnLocalException
import com.gap.gradle.extensions.GapWMSegmentDslAction
import com.gap.gradle.utils.RetryCommand
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
	def segmentDsl
	def projectName = "WM Temporary Procedures"

	int TIME_TO_WAIT_IN_MINUTES = 30
	int INTERVAL_IN_MINUTES = 1

	CreateECProcedureTask(Project project, commanderClient = new CommanderClient()) {
		super(project)
		this.project = project
		this.commanderClient = commanderClient
		this.segmentDsl = project.segment
	}

	def execute() {
		executeCreateDynamicNodes()
		segmentPhases.each { phase ->
			createPhaseProcedure(phase)
		}
	}

	def createPhaseProcedure(phase) {
		def procedureName = "perform_${phase}_actions_" + commanderClient.getJobId()
		def createProcConfig = [description: 'dynamic procedure created by gradle task gap_wm_segmentdsl']
		commanderClient.createProcedure(projectName, procedureName, createProcConfig)
		def phasePropertyName = phase.toString().replaceAll("_", "")
		commanderClient.setECProperty("/myJob/watchmen_config/${phasePropertyName}StepProcedureName", procedureName)
		logger.info("segmentdsl phase actions: " + segmentDsl[phase])
		segmentDsl[phase].each { dslAction ->
			logger.info("phase dsl action: " + dslAction)
			createPhaseECStep(procedureName, dslAction)
		}
	}

	def createPhaseECStep(procedureName, o) {
		def ecStepConfig = [:]
		def subProject, subProcedure
		assert o instanceof GapWMSegmentDslAction
		GapWMSegmentDslAction dsl = (GapWMSegmentDslAction) o
		logger.info("processing segment step:" + dsl.toString())
		def stepName = "Perform ${dsl.name}: ${dsl.getStepName()}"
		if (dsl.getAction()) {
			if (dsl.hasSubProject()) {
				(subProject, subProcedure) = dsl.getProjectAndProcedure()
			} else {
				(subProject, subProcedure) = [projectName, dsl.getAction().toString()]
			}
			def subProjectName = checkPromotedPlugin(subProject)
			logger.info("Creating Step in ($projectName:$procedureName). stepName: '$stepName' (Delegating to: ${subProjectName}:${subProcedure})")

			ecStepConfig.put('subproject', subProjectName)
			ecStepConfig.put('subprocedure', subProcedure)
			ecStepConfig.put('actualParameter', dsl.getECParameters().split(";"))

			// check Lock Resource on local
			checkLockResourceOnLocal(dsl)
		} else if (dsl.getCommand()) {
			ecStepConfig.put('command', dsl.command)
		}

		ecStepConfig.put('resourceName', dsl.getResourceName().toString())
		ecStepConfig.put('workspaceName', dsl.getWorkspaceName().toString())
		ecStepConfig.put('condition', dsl.getECStepRunCondition(commanderClient))
		ecStepConfig.put('parallel', dsl.getECParallelStep())
		logger.info("Step Config: " + ecStepConfig.toString())
		commanderClient.createStep(projectName, procedureName, stepName, ecStepConfig)
	}

	def checkLockResourceOnLocal(GapWMSegmentDslAction dsl) {
		if (!dsl.hasSubProject()) return
		def errorMsg = "Cannot use local resource for locking. Local Resource used. Please define a non-local resource for locking."
		def (project, procedure) = dsl.getProjectAndProcedure()
		if ('WM Segment'.equalsIgnoreCase(project) && 'Lock Resource'.equalsIgnoreCase(procedure)) {
			if (dsl.getParameters().isEmpty()) {
				logger.error(errorMsg)
				throw new WMSegmentDslLockResourceOnLocalException(errorMsg)
			}
			dsl.getParameters().each { param ->
				if ('host'.equalsIgnoreCase(param.name.toString())) {
					def definedHost = param.value.toString().trim()
					if (definedHost.isEmpty() || 'local'.equalsIgnoreCase(definedHost)) {
						logger.error(errorMsg)
						throw new WMSegmentDslLockResourceOnLocalException(errorMsg)
					}
				}
			}
		}
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
		} catch (all) {
			logger.error(all)
		}

		return promotedPlugin
	}

	def executeCreateDynamicNodes() {
		def easyCreateParams
		def projectName = "Nova-CLI"
		def procedureName = "Easy Create"
		def nodeList = []
		segmentDsl.dynamicNodes.each { node ->
			easyCreateParams = [:]
			easyCreateParams.put("tenant", "${node.openstackTenant}".toString())
			easyCreateParams.put("roleName", "${node.chefRole}".toString())
			easyCreateParams.put("hostname", "${node.name}".toString())

			easyCreateParams.put("network", "public".toString())
			easyCreateParams.put("autoPurge", "true")
			easyCreateParams.put("createResource", "true")
			easyCreateParams.put("type", "${node.imageType}".toString())

			logger.info("Initializing Create Dynamic Node:  ${node.name} on ${node.openstackTenant} tenant with ${node.chefRole} role.")
			def jobId = commanderClient.runProcedure(projectName, procedureName, easyCreateParams)
			logger.info("Dynamic Node ${node.name} has JobId $jobId")
			def currentNode = [:]
			currentNode.jobId = jobId
			currentNode.node = node
			nodeList.add(currentNode)
		}

		try {
			if (waitForJobToComplete(nodeList)) {
				nodeList.each { eachNode ->
					if (!'successful'.equalsIgnoreCase(commanderClient.getJobStatus(eachNode.jobId).outcome.toString())) {
						throw new DynamicNodesException("Problematic node:  ${eachNode.node.name} on ${eachNode.node.openstackTenant} tenant with ${eachNode.node.chefRole} role.")
					}
				}
				logger.info("All nodes successfully created")
			}
		}
		catch (DynamicNodesException de) {
			nodeList.each { eachNode ->
				deleteNode(eachNode.node)
			}
			throw de
		}
	}

	def waitForJobToComplete(def nodeList) {

		try {
			RetryCommand.executeWithRetry(TIME_TO_WAIT_IN_MINUTES, INTERVAL_IN_MINUTES, {
				nodeList.each { eachNode ->
					if ('error'.equalsIgnoreCase(commanderClient.getJobStatus(eachNode.jobId).outcome.toString())) {
						def errorText="Error creating Dynamic node:  ${eachNode.node.name} on ${eachNode.node.openstackTenant} tenant with ${eachNode.node.chefRole} role."
						logger.error(errorText)
						throw new DynamicNodesException(errorText)
					}
				}

				boolean statusCompleted = true
				nodeList.each { eachNode ->
					statusCompleted = statusCompleted && 'completed'.equalsIgnoreCase(commanderClient.getJobStatus(eachNode.jobId).status.toString())
					logger.info("Dynamic Node : ${eachNode.node.name}  on ${eachNode.node.openstackTenant} tenant with ${eachNode.node.chefRole} with status ${commanderClient.getJobStatus(eachNode.jobId).status})")
				}
				if (statusCompleted) {
					logger.info("All nodes status completed")
				}
				return statusCompleted
			})
			return true
		}
		catch (Exception ex) {
			logger.error(ex)
			return false
		}

	}

	def deleteNode(def node) {
		def projectName = "Nova-CLI"
		def procedureName = "Easy Delete"
		def easyCreateParams = [:]
		easyCreateParams.put("resourceToDelete", "${node.name}".toString())
		easyCreateParams.put("tenant", "${node.openstackTenant}".toString())
		commanderClient.runProcedure(projectName, procedureName, easyCreateParams)
		logger.info("Deleted node :  ${node.name} on ${node.openstackTenant} tenant.")
	}

}
