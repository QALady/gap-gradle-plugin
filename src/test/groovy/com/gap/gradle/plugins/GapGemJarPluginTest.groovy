package com.gap.gradle.plugins

import static org.junit.Assert.assertThat
import static org.hamcrest.CoreMatchers.is

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class GapGemJarPluginTest {

    @Test
    public void shouldAddGemJarRepoToProject(){
     def project = ProjectBuilder.builder().build()
     project.apply plugin: 'gapgemjar'
     assertThat(project.repositories.first().url.toString(), is('http://artifactory.gapinc.dev/artifactory/gemjars'))
    }
}
