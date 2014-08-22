package com.gap.gradle.plugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

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
/*
    @Test
    public void isZipCreated(){

        def task = project.tasks.findByName('watchmenUploadFunctionalTests')
        task.execute()
    }
*/

/*
    @Test
    public void isZipUploaded(){

        def task = project.tasks.findByName('watchmenUploadFunctionalTests')
        task.execute()
    }
*/
}
