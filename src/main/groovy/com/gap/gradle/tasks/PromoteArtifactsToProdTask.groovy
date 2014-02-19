package com.gap.gradle.tasks

import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

@RequiredParameters([
@Require(parameter = 'artifactCoordinates', description = 'location to download artifacts from'),
@Require(parameter = 'artifactConfiguration', description = 'ivy configuration to download artifacts from'),
@Require(parameter = 'toArtifactCoordinates', description = 'ivy configuration to upload the artifacts')
])

class PromoteArtifactsToProdTask extends WatchmenTask {

    private def project

    PromoteArtifactsToProdTask (project){
        super(project)
        this.project = project
    }

    def execute(){
        validate()
        def downloadDir = "${project.rootDir}/downloads/"
        new File(downloadDir).mkdirs()
        project.tasks.findByName('downloadArtifacts').execute()
        def artifactsDir = "${project.rootDir}/build/artifacts"
        new File(artifactsDir).mkdirs()
        project.copy {
            from("${downloadDir}"){
                include '*'
            }
            into "${artifactsDir}"
        }
        project.tasks.findByName('uploadBuildArtifacts').execute()
    }
}
