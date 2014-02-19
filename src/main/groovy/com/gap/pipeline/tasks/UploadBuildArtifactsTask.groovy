package com.gap.pipeline.tasks

import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import com.gap.pipeline.utils.IvyCoordinateParser
import org.apache.commons.logging.LogFactory

@RequiredParameters([
    @Require(parameter = "artifactCoordinates", description = "Location where the artifact is to uploaded. The format is <groupname>:<modulename>")
])
class UploadBuildArtifactsTask extends com.gap.pipeline.tasks.WatchmenTask {
    def project
    def log = LogFactory.getLog(com.gap.pipeline.tasks.UploadBuildArtifactsTask)

    UploadBuildArtifactsTask(project){
        super(project)
        this.project = project
    }
    def validate(){
        super.validate()

        def coordinates = new IvyCoordinateParser().parse(project.artifactCoordinates)

        def moduleName = coordinates.name
        if (!project.name.equals(moduleName)){
            //for ivy to upload tup;o the right location, the folder name should match the ivy module name.
            throw new IllegalArgumentException("The module name in archiveLocation['${moduleName}'] does not match project name['${project.name}'].")
        }

    }
    def execute(){
        log.info("Executing task uploadBuildArtifacts...")
        validate()
        def coordinates = new IvyCoordinateParser().parse(project.artifactCoordinates)
        project.group = coordinates.group
        project.version = coordinates.version
        log.info("Uploading artifacts to  - ${coordinates} ...")
        addBuildArtifactsToArchives()
        uploadArtifactsToIvy()
    }

    private void uploadArtifactsToIvy() {
        project.uploadArchives.repositories {
            ivy {
                layout "maven"
                url project.ivy.url
                credentials {
                    username project.ivy.userName
                    password project.ivy.password
                }
            }
        }
        project.uploadArchives.execute()
    }

    private void addBuildArtifactsToArchives() {
        project.configurations {
            archives
        }

        project.artifacts {
            def artifactsDir = new File("${project.buildDir}/artifacts")
            artifactsDir.listFiles().each { file ->
                archives file
            }
        }
    }

}
