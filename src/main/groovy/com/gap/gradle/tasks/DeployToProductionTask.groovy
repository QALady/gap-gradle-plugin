package com.gap.gradle.tasks
import com.gap.gradle.exceptions.DeployToProductionException;
import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

class DeployToProductionTask {

    Log log = LogFactory.getLog(DeployToProductionTask)
    Project project

    DeployToProductionTask(Project project) {
        this.project = project
    }

    def execute() {
        validate()
        deployToNodes()
    }

    def validate() {
        if (!project.prodDeploy.nodes) {
            throw new IllegalStateException("No nodes configured on project!")
        }
    }

    def deployToNodes() {
        if (System.getenv("DISABLE_DEPLOY_TO_PROD")) {
            log.info("Skipping deploy to prod (DISABLE_DEPLOY_TO_PROD is set)")
        } else {
            def command = new ShellCommand()
            project.prodDeploy.nodes.each { node ->
                try {
                    log.info("Deploying to '${node}'...")
                    log.info(command.execute("ssh ${node} 'chef-client'".toString()))
                } catch (ShellCommandException exception) {
                    log.error("Failed to deploy on node '${node}'", exception)
                    throw new DeployToProductionException("Failed to deploy on node '${node}'", exception)
                }
            }
        }
    }
}
