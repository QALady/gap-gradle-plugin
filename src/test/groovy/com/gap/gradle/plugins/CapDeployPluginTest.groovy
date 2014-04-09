package com.gap.gradle.plugins

import static helpers.Assert.taskShouldExist

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class CapDeployPluginTest {

    Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'cap-deploy'
    }



    @Test
    public void shouldAddDeployTask(){
        taskShouldExist('deploy', project)
    }
}
