package com.gap.gradle.plugins.cookbook
import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.nullValue
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat

import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GapCookbookPluginTests {

    @Rule
    public final ConfigFileResource config = new ConfigFileResource(GapCookbookPlugin, "CONFIG_FILE")

    private Project project

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
    }

    @Test
    void publishCookbookToArtifactoryTaskIsAddedToProject(){
        def task = project.tasks.findByName('publishCookbookToArtifactory')
        assertEquals('publishCookbookToArtifactory', task.name)
    }

    @Test
    void publishCookbookToChefServerTaskIsAddedToProject () {
        def task = project.tasks.findByName('publishCookbookToChefServer')
        assertEquals('publishCookbookToChefServer', task.name)
    }

    @Test
    void shouldExecutePublishCookbookToChefServerTask (){
        def mockTask = new MockFor(PublishCookbookToChefServerTask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('publishCookbookToChefServer')
        mockTask.use {
            task.execute()
        }
    }

    @Test
    void shouldExecutePublishCookbookToArtifactoryTask (){
        def mockTask = new MockFor(PublishCookbookToArtifactoryTask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('publishCookbookToArtifactory')
        mockTask.use {
            task.execute()
        }
    }

    @Test
    void shouldUseDefaultConfig_whenConfigFileDoesNotExist() {
        new File(GapCookbookPlugin.CONFIG_FILE).delete()
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        assertThat(project.jenkins.serverUrl, nullValue())
        assertThat(project.chef.environment, equalTo('tdev'))
    }

    @Test
    void shouldReadCredentialsFromConfigFile() {
        new File(GapCookbookPlugin.CONFIG_FILE).write(
            "jenkins.serverUrl=http://my.jenkins.server\n"
            + "chef.environment=prod"
        )
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        assertThat(project.jenkins.serverUrl, equalTo("http://my.jenkins.server"))
        assertThat(project.chef.environment, equalTo("prod"))
    }
}
