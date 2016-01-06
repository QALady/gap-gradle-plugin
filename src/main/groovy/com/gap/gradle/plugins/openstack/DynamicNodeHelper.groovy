package com.gap.gradle.plugins.openstack

import org.apache.commons.logging.LogFactory

import com.gap.gradle.exceptions.DynamicNodesException
import com.gap.gradle.exceptions.RetryTimeOutException;
import com.gap.gradle.utils.RetryCommand
import com.gap.pipeline.ec.CommanderClient

class DynamicNodeHelper {
	def logger = LogFactory.getLog(DynamicNodeHelper)
	private CommanderClient commanderClient
	public static def TIME_TO_WAIT_IN_MINUTES = 30
	public static def INTERVAL_IN_MINUTES = 1
	
	DynamicNodeHelper(commanderClient = new CommanderClient()) {
		this.commanderClient = commanderClient
	}
	
	public def createDynamicNodes(dynamicNodesDSL) {
		def easyCreateParams
		def projectName = "Nova CLI"
		def procedureName = "Easy Create"
		def nodeList = []
		dynamicNodesDSL.each { node ->
			easyCreateParams = [:]
			easyCreateParams.put("tenant", "${node.openstackTenant}".toString())
			easyCreateParams.put("roleName", "${node.chefRole}".toString())
			easyCreateParams.put("hostname", "${node.name}".toString())
			easyCreateParams.put("network", "public".toString())
			easyCreateParams.put("autoPurge", "true")
			easyCreateParams.put("createResource", "true")
			easyCreateParams.put("type", "${node.imageType}".toString())

			def jobId = commanderClient.runProcedure(projectName, procedureName, easyCreateParams)
			logger.info("Triggered Job with JobId $jobId to create Dynamic Node ${node.name} on ${node.openstackTenant} tenant with ${node.chefRole} role.")

			def currentNode = [:]
			currentNode.jobId = jobId
			currentNode.node = node
			nodeList.add(currentNode)
		}

		try {
			if (waitForJobToComplete(nodeList)) {
				nodeList.each { eachNode ->
					if (!'success'.equalsIgnoreCase(commanderClient.getJobStatus(eachNode.jobId).outcome.toString())) {
						throw new DynamicNodesException("Error: Job with JobId ${eachNode.jobId} errored out in creating node ${eachNode.node.name} on ${eachNode.node.openstackTenant} tenant with ${eachNode.node.chefRole} role.")
					}
				}
				logger.info("All dynamic nodes successfully created")
			}
		}
		catch (DynamicNodesException de) {
			nodeList.each { eachNode ->
				deleteNode(eachNode.node)
			}
			throw de
		}
	}

	private def waitForJobToComplete(def nodeList) {
			try {
				RetryCommand.executeWithRetry(TIME_TO_WAIT_IN_MINUTES, INTERVAL_IN_MINUTES, {
					nodeList.each { eachNode ->
						if ('error'.equalsIgnoreCase(commanderClient.getJobStatus(eachNode.jobId).outcome.toString())) {
							def errorText = "Error: Job with JobId ${eachNode.jobId} errored out in creating node ${eachNode.node.name} on ${eachNode.node.openstackTenant} tenant with ${eachNode.node.chefRole} role."
							logger.error(errorText)
							throw new DynamicNodesException(errorText)
						}
					}
	
					boolean statusCompleted = true
					nodeList.each { eachNode ->
						statusCompleted = statusCompleted && 'completed'.equalsIgnoreCase(commanderClient.getJobStatus(eachNode.jobId).status.toString())
						logger.info("(Dynamic Node: ${eachNode.node.name}  with JobId ${eachNode.jobId} tenant with ${eachNode.node.chefRole} with status ${commanderClient.getJobStatus(eachNode.jobId).status})")
					}
					if (statusCompleted) {
						logger.info("All dynamic node Easy Create Jobs are completed")
					}
					return statusCompleted
				})
				return true
			}
			catch (RetryTimeOutException ex) {
				logger.error(ex)
				throw new DynamicNodesException(ex.getMessage())
			}
		}
		
		def deleteNode(def node) {
			def projectName = "Nova CLI"
			def procedureName = "Easy Delete"
			def easyCreateParams = [:]
			easyCreateParams.put("resourceToDelete", "${node.name}".toString())
			easyCreateParams.put("tenant", "${node.openstackTenant}".toString())
			def jobId = commanderClient.runProcedure(projectName, procedureName, easyCreateParams)
			logger.info("Triggered Job with JobId ${jobId} to deleted node ${node.name} on ${node.openstackTenant} tenant.")
		}
		

}
