package com.gap.gradle.tasks

import com.gap.pipeline.tasks.DownloadArtifactsTask
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import com.gap.pipeline.utils.IvyCoordinateParser

@RequiredParameters([
@Require(parameter = 'fromCoordinates', description = 'location to download artifacts from'),
@Require(parameter = 'fromConfiguration', description = 'ivy configuration to download artifacts from'),
@Require(parameter = 'toCoordinates', description = 'ivy configuration to upload the artifacts'),
@Require(parameter = 'toArtifactoryUrl', description = 'destination url where artifacts need to be uploaded'),
])

class PromoteArtifactsTask extends WatchmenTask {

    private def project

    PromoteArtifactsTask(project){
        super(project)
        this.project = project
    }

    def execute(){
        validate()
        def downloadDir = "${project.rootDir}/downloads/"
        downloadArtifacts(downloadDir)
        uploadArtifacts(downloadDir)
    }

    private uploadArtifacts(downloadDir) {
        def artifactsDir = "${project.rootDir}/build/artifacts"
        new File(artifactsDir).mkdirs()
        project.copy {
            from("${downloadDir}") {
                include '*'
            }
            into "${artifactsDir}"
        }
        def ivy = new IvyCoordinateParser().parse(project.fromCoordinates)
        //setting up project parameters required by uploadBuildArtifacts task
        project.artifactCoordinates = "${project.toCoordinates}:${ivy.version}"
        project.ivy.url = project.toArtifactoryUrl
        project.ivy.checkIfExists = true
        project.tasks.findByName('uploadBuildArtifacts').execute()
        project.ivy.checkIfExists = false
    }

    private downloadArtifacts(downloadDir) {
        new File(downloadDir).mkdirs()
        //setting up project parameters required by downloadArtifacts task
        configureParametersForDownloadTask()
        project.tasks.findByName('downloadArtifacts').execute()
    }

    private configureParametersForDownloadTask() {
        project.metaClass.artifactCoordinates = project.fromCoordinates
        project.metaClass.artifactConfiguration = project.fromConfiguration
        project.metaClass.destination = "${project.rootDir}/downloads"
    }

    def configure() {
        configureParametersForDownloadTask()
        new DownloadArtifactsTask(project).configure()
    }
}
