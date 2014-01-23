package com.gap.gradle.plugins

import static org.hamcrest.Matchers.equalTo
import static org.hamcrest.Matchers.nullValue
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat

import com.gap.gradle.plugins.cookbook.GapCookbookPlugin
import com.gap.gradle.plugins.cookbook.PublishCookbookToArtifactoryTask
import com.gap.gradle.plugins.cookbook.PublishCookbookToChefServerTask
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GapCookbookPluginTests {

    @Rule
    public final TemporaryFolder temp = new TemporaryFolder()

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
        def task = project.tasks.findByName('publishCookbookToChefServer2')
        assertEquals('publishCookbookToChefServer2', task.name)
    }

    @Test
    void shouldExecutePublishCookbookToChefServerTask (){
        def mockTask = new MockFor(PublishCookbookToChefServerTask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('publishCookbookToChefServer2')
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
        def originalConfig = GapCookbookPlugin.CONFIG_FILE
        def config = "/this/does/not/exist.properties"
        try {
            GapCookbookPlugin.CONFIG_FILE = config
            project = ProjectBuilder.builder().build()
            project.apply plugin: 'gapcookbook'
            assertThat(project.jenkins.serverUrl, nullValue())
            assertThat(project.chef.environment, equalTo('tdev'))
        } finally {
            GapCookbookPlugin.CONFIG_FILE = originalConfig
        }
    }

    @Test
    void shouldReadCredentialsFromConfigFile() {
        def originalConfig = GapCookbookPlugin.CONFIG_FILE
        def config = temp.newFile()
        config.write("jenkins.serverUrl=http://my.jenkins.server\nchef.environment=prod")
        try {
            GapCookbookPlugin.CONFIG_FILE = config.absolutePath
            project = ProjectBuilder.builder().build()
            project.apply plugin: 'gapcookbook'
            assertThat(project.jenkins.serverUrl, equalTo("http://my.jenkins.server"))
            assertThat(project.chef.environment, equalTo("prod"))
        } finally {
            GapCookbookPlugin.CONFIG_FILE = originalConfig
        }
    }
}
