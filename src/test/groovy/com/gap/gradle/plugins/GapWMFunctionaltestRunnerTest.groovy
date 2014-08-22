package com.gap.gradle.plugins

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static helpers.Assert.taskShouldExist

class GapWMFunctionaltestRunnerTest {

    Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gap-wm-functionaltest-runner'
    }

    @Test
    public void uploadFunctionalTestsShoulExistTask(){
        taskShouldExist('watchmenUploadFunctionalTests', project)
    }
/*
    @Test
    public void zipIsCreatedTask(){

        def task = project.tasks.findByName('watchmenUploadFunctionalTests')
        task.execute()
    }
*/

}
