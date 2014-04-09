package com.gap.gradle.plugins

import static org.junit.Assert.assertThat

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

import static org.hamcrest.CoreMatchers.is

class GapBuildPluginTest {


    @Test
    public void shouldApplyGapResourcesPluginIfJavaIsApplied(){
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'java'
        project.apply plugin: 'gapbuild'
        assertThat(project.plugins.hasPlugin('gapresources'), is(true))
    }

    @Test
    public void shouldNotApplyGapResourcesPluginIfJavaIsNotApplied(){
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapbuild'
        assertThat(project.plugins.hasPlugin('gapresources'), is(false))
    }

}
