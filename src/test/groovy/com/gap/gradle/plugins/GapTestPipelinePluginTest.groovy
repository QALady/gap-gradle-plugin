package com.gap.gradle.plugins

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import static org.junit.Assert.assertTrue

import static helpers.Assert.taskShouldExist

class GapTestPipelinePluginTest {

    Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gap-test-pipeline'
    }

    @Test
    public void UploadFunctionalTestsTaskShouldExist(){
        taskShouldExist('uploadFunctionalTestsTask', project)
    }

    @Test
    public void isZipCreated(){

        def task = project.tasks.findByName('uploadFunctionalTestsTask')

        //we need to create the folder functional test for the test project
        //and add a dummy file
        File createFolder = new File("${project.projectDir}/functional-tests")
        FileUtils.forceMkdir(createFolder)
        File createFiles = new File("${project.projectDir}/functional-tests/something.txt")
        FileUtils.writeStringToFile(createFiles,"test")

        task.execute()

        File theZip = new File("${project.projectDir}/functional-tests/functional-tests.zip")
     //   assertTrue("The functional test artifact file was not created", theZip.isFile())
    }


/*
    @Test
    public void isZipUploaded(){

        def task = project.tasks.findByName('watchmenUploadFunctionalTests')
        task.execute()
    }
*/
}
