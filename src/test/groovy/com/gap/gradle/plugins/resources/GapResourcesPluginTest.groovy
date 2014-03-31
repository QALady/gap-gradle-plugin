package com.gap.gradle.plugins.resources

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test




class GapResourcesPluginTest {

    @Test
    public void shouldAddProcessTemplatesTaskForEachSourceSet(){
        def project = ProjectBuilder.builder().build();
        project.apply plugin: 'java'
        project.apply plugin: 'gapresources'
        def task = project.tasks.findByName('processTemplates')
    }
}
