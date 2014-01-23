package com.gap.gradle.plugins
import static org.junit.Assert.assertEquals

import com.gap.gradle.plugins.gapcookbook.PublishCookbookToArtifactoryTask
import com.gap.gradle.plugins.gapcookbook.PublishCookbookToChefServerTask
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GapCookbookPluginTests {
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



}
