package com.gap.gradle.plugins.openstack

import org.apache.commons.logging.LogFactory

import com.gap.gradle.exceptions.DynamicNodesException
import com.gap.gradle.exceptions.RetryTimeOutException;
import com.gap.gradle.utils.RetryCommand
import com.gap.pipeline.ec.CommanderClient

class DynamicNodeHelper {
	def logger = LogFactory.getLog(DynamicNodeHelper)
	private CommanderClient commanderClient
	public static int TIME_TO_WAIT_IN_MINUTES = 30
	public static int INTERVAL_IN_MINUTES = 1
	
	DynamicNodeHelper(commanderClient = new CommanderClient()) {
		this.commanderClient = commanderClient
	}
	
	public def createDynamicNodes(dynamicNodesDSL) {
		def easyCreateParams
		def projectName = "Nova-CLI"
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

	private def waitForJobToComplete(def nodeList) {
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
			catch (RetryTimeOutException ex) {
				logger.error(ex)
				throw new DynamicNodesException(ex.getMessage())
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
