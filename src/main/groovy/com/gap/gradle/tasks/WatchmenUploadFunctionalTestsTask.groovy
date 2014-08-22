package com.gap.gradle.tasks

import com.gap.pipeline.tasks.WatchmenTask
import org.gradle.api.Project

class WatchmenUploadFunctionalTestsTask extends WatchmenTask {
    Project project
    public WatchmenUploadFunctionalTestsTask(Project project){
        super(project)
        this.project = project
    }
    def execute() {
        makeAZip()
    }

/*
    private void makeAZip(){
        task ZipFT(type: Zip) {
            archiveName : "functional-test.zip"
            destinationDir: project.getProjectDir().text
            from ("${project.projectDir}/functional-tests"){
                include '** / *'
            }
        }

    }
    */
}
