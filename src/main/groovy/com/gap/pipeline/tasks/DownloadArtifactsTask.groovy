package com.gap.pipeline.tasks

import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import com.gap.pipeline.utils.IvyCoordinateParser
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

@RequiredParameters([
@Require(parameter = 'artifactCoordinates', description = 'location to download artifacts from'),
@Require(parameter = 'artifactConfiguration', description = 'ivy configuration to download artifacts from'),
@Require(parameter = 'destination', description = 'folder to download the artifacts to.')
])
class DownloadArtifactsTask extends com.gap.pipeline.tasks.WatchmenTask {
    def log = LogFactory.getLog(com.gap.pipeline.tasks.DownloadArtifactsTask)
    private project

    DownloadArtifactsTask(project) {
        super(project)
        this.project = project
    }

    def configure() {
        log.info("Configuring DownloadArtifacts task...")

        def ivyParser = new IvyCoordinateParser()
        validateParameters(project, ivyParser)

        project.configurations.create("_watchmenInternal")
        def ivyCoordinates = ivyParser.parse(project.artifactCoordinates)
        log.info("Configuring artifacts to be downloaded from ${ivyCoordinates}")
        project.dependencies.add('_watchmenInternal', [group: ivyCoordinates.group, name: ivyCoordinates.name, version: ivyCoordinates.version, configuration: project.artifactConfiguration])
        return project
    }

    def validateParameters(Project project, IvyCoordinateParser ivyParser) {
        validate()
        ivyParser.validate(project.artifactCoordinates)
    }

    def execute() {
        log.info("Executing DownloadDependencyTask...")
        log.info("Copying downloaded dependencies to ${project.destination}")
        project.copy {
            from project.configurations._watchmenInternal
            into project.destination
            rename(/([^-]+).+(\.[a-z]+)/, '$1$2')
        }
    }

}
