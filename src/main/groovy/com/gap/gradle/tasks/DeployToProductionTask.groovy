package com.gap.gradle.tasks

import com.gap.gradle.exceptions.DeployToProductionException
import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

@RequiredParameters([
        @Require(parameter = 'prodDeploy.nodes', description = 'List of nodes to be to be deployed')
])
class DeployToProductionTask extends WatchmenTask {

    Log log = LogFactory.getLog(DeployToProductionTask)
    Project project

    DeployToProductionTask(Project project) {
        super(project)
        this.project = project
    }

    def execute() {
        super.validate()
        deployToNodes()
    }

    def deployToNodes() {
        if (System.getenv("DISABLE_DEPLOY_TO_PROD")?.toBoolean()) {
            log.info("Skipping deploy to prod (DISABLE_DEPLOY_TO_PROD is set...)")
        } else {
            def command = new ShellCommand()
            def commandToExecute
            project.prodDeploy.nodes.each { node ->
                try {
                    commandToExecute = "ssh ${node} sudo chef-client"
                    log.info("Deploying to '${node}'...")
                    log.info("Executing command '${commandToExecute}' on node '${node}' ....")
                    log.info(command.execute(commandToExecute.toString()))
                } catch (ShellCommandException exception) {
                    log.error("Failed to deploy on node '${node}'.. Failed to execute command '${commandToExecute}'...", exception)
                    throw new DeployToProductionException("Failed to deploy on node '${node}'".toString(), exception)
                }
            }
        }
    }
}
