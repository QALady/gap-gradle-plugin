package com.gap.gradle.plugins.git

import com.gap.gradle.plugins.cookbook.UpdateCookbookSHATask
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertThat

class GapChefPluginTests {

    private Project project

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapchef'
    }

    @Test
    void shouldUseGitConfig_whenConfigIsSet(){
        setupTaskProperties()
        assertEquals(project.gitconfig.userId, "testUser")
        assertEquals(project.gitconfig.fullRepoName, "test/Repo")
        assertEquals(project.gitconfig.shaId, "testSHA")
    }

    @Test
    void shouldExecuteGitUpdateSHATask(){
        setupTaskProperties()
        def mockTask = new MockFor(UpdateCookbookSHATask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('promoteCookbookBerksfile')
        mockTask.use {
            task.execute()
        }
    }

    @Test
    void gitUpdateCookbookSHATaskIsAddedToProject(){
        taskShouldExist('promoteCookbookBerksfile')
    }

    private void setupTaskProperties(){
        project.gitconfig.userId = "testUser"
        project.gitconfig.fullRepoName = "test/Repo"
        project.gitconfig.shaId = "testSHA"
    }

    def taskShouldExist(task) {
        assertThat(project.tasks.findByName(task), notNullValue())
    }
}
