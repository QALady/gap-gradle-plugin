package com.gap.gradle.plugins.resources

import static helpers.Assert.taskShouldExist

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GapResourcesPluginTest {

    @Test
    public void shouldAddProcessTemplatesTaskForEachSourceSet(){
        Project project = ProjectBuilder.builder().build();
        project.apply plugin: 'java'
        project.apply plugin: 'gapresources'
        project.sourceSets.add('blah')
        taskShouldExist('processTemplates', project)
        taskShouldExist('processBlahTemplates', project)
    }
}
