package com.gap.gradle.plugins

import org.junit.Ignore

import static helpers.Assert.taskShouldExist

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

@Ignore
class GapDeliverPluginTest {

    Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapdeliver'
    }

    @Test
    public void shouldAddDeliverAppTask(){
        taskShouldExist('deliverApp', project)
    }
}
