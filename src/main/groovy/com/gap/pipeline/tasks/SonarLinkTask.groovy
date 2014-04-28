package com.gap.pipeline.tasks
import com.gap.pipeline.ec.CommanderClient

class SonarLinkTask {

    private final def project
    private final def commander

    SonarLinkTask(project, commander = new CommanderClient()){
        this.commander = commander
        this.project = project

    }
    def execute() {
        if(commander.isRunningInPipeline()){
            commander.addLinkToUrl("Sonar Dashboard", "${getSonarProperty("sonar.host.url")}dashboard/index/${getSonarProperty("sonar.projectKey")}")
        }
    }

    private def getSonarProperty(String key) {
        project.tasks.sonarRunner.sonarProperties.getProperty(key)
    }
}
