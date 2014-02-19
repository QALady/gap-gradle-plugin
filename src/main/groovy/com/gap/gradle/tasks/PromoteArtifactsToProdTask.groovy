package com.gap.gradle.tasks

class PromoteArtifactsToProdTask {

    private def project

    PromoteArtifactsToProdTask (project){
        this.project = project
    }

    def execute(){
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
