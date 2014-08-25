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
    }

    @Test
    public void shouldVerify_AllTasksExists(){
        project.apply plugin: 'java'
        project.apply plugin: 'gap-test-pipeline'
        taskShouldExist('packageFunctionalTests', project)
        taskShouldExist('uploadFunctionTests', project)
    }

 /*   @Test
    public void shouldRun_AllTasks(){
        project.apply plugin: 'java'
        project.apply plugin: 'gap-test-pipeline'
        def task = project.tasks.findByName("uploadFunctionTests")
        task.execute()
    }

  */
}
