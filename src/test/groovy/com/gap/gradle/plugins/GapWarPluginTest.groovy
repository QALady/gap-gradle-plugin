package com.gap.gradle.plugins

import static helpers.Assert.taskShouldExist
import static helpers.Assert.taskShouldNotExist

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GapWarPluginTest {

    @Test
    public void shouldAddDeployToTomcatTaskIfWarPluginIsApplied(){
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'war'
        project.apply plugin: 'gapwar'
        taskShouldExist('deployToTomcat', project)
    }

    @Test
    public void shouldNotAddDeployToTomcatTaskIfWarPluginIsNotApplied(){
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapwar'
        taskShouldNotExist('deployToTomcat', project)
    }

}
