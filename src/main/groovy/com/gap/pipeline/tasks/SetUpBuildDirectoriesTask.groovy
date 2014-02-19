package com.gap.pipeline.tasks

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class SetUpBuildDirectoriesTask {
    def project
    Log log = LogFactory.getLog(com.gap.pipeline.tasks.SetUpBuildDirectoriesTask)

    SetUpBuildDirectoriesTask(project){
        this.project = project
    }

    def execute() {
        log.info("Executing Task SetUpBuildDirectories...")

        def buildArtifacts = new File("${project.buildDir}/artifacts".toString())
        buildArtifacts.mkdirs()

        def buildReports = new File("${project.buildDir}/reports".toString())
        buildReports.mkdirs()
    }
}
