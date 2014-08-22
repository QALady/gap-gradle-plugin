package com.gap.gradle.tasks

import com.gap.pipeline.tasks.WatchmenTask
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import java.util.zip.ZipOutputStream
import java.util.zip.ZipEntry


class UploadFunctionalTestsTask extends WatchmenTask {
    Project project
    public UploadFunctionalTestsTask(Project project){
        super(project)
        this.project = project
    }

    def execute() {
        makeAZip()
    }

    private void makeAZip(){

        project.task("ZipFT", type: Zip){
            archiveName : "functional-tests.zip"
            destinationDir: "${project.projectDir}"
            from ("${project.projectDir}/functional-tests"){
                include '** /*'
            }
        }
        println project.projectDir
        def task = project.tasks.findByName('ZipFT')
        task.execute()
    }
      /*
        String zipFileName = "functional-tests.zip"
        String inputDir = "${project.projectDir}/functional-tests"

        ZipOutputStream zipFile = new ZipOutputStream(new FileOutputStream(zipFileName))
        new File(inputDir).eachFile() { file ->
            println file
            zipFile.putNextEntry(new ZipEntry(file.getName()))
            def buffer = new byte[1024]
            file.withInputStream { i ->
                def l = i.read(buffer)
            // check wether the file is empty
            //    if (l > 0) {
                    zipFile.write(buffer, 0, l)
            //    }
            }
            zipFile.closeEntry()
        }
        zipFile.close()
    }
/*
    private void uploadAZip(){
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
