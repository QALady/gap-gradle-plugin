package com.gap.gradle.plugins
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GapSonarRunnerPluginTest {
    private Project project

    @Before
    public void setUp(){
        project = ProjectBuilder.builder().build()
    }

    @Test
    public void shouldApplySonarRunnerPlugin(){
        project.apply plugin: 'gap-sonar-runner'
        assertThat(project.plugins.hasPlugin('sonar-runner'), is(true))
    }

}
