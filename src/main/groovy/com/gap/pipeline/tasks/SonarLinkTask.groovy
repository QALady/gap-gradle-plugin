package com.gap.pipeline.tasks

import com.gap.pipeline.ec.CommanderClient
import org.slf4j.LoggerFactory

class SonarLinkTask {

    private final def project
    private final def commander
    private final def logger = LoggerFactory.getLogger(com.gap.pipeline.tasks.SonarLinkTask)

    SonarLinkTask(project, commander = new CommanderClient()) {
        this.commander = commander
        this.project = project
    }

    def execute() {
        if (commander.isRunningInPipeline()) {
            def url = "${getSonarProperty("sonar.host.url")}dashboard/index/${getSonarProperty("sonar.projectKey")}"
            logger.info("creating link to sonar dashboard - ${url}")
            commander.addLinkToUrl("Sonar Dashboard", url)
        } else {
            logger.info("skipping link creation because this task is not running in the context of an EC job.")
        }
    }

    private def getSonarProperty(String key) {
        project.tasks.sonarRunner.sonarProperties.get(key)
    }
}
