package com.gap.gradle.plugins.git

import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.Matchers.notNullValue
import static org.hamcrest.Matchers.nullValue
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

/**
 * Created by sh6o2vv on 2/6/14.
 */
class GapGitPluginTests {

    private Project project

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapgit'
    }

    @Test
    void gitCheckoutTaskIsAddedToProject(){
        taskShouldExist('gitCheckout')
    }

    @Test
    void gitUpdateSHATaskIsAddedToProject(){
        taskShouldExist('gitUpdateSHA')
    }

    @Test
    void gitCommitAndPushTaskIsAddedToProject(){
        taskShouldExist('gitCommitAndPush')
    }

    @Test
    void  gitUpdateSHATaskShouldDependOnGitCheckoutTask(){
        taskShouldDependOn('gitUpdateSHA','gitCheckout')
    }

    @Test
    void shouldUseDefaultGitConfig_whenConfigNotPassed(){
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapgit'
        assertThat(project.gitconfig.userId, nullValue())
        assertThat(project.gitconfig.fullRepoName, nullValue())
        assertThat(project.gitconfig.shaId, nullValue())
    }

    @Test
    void shouldExecuteGitCheckoutTask(){
        setupTaskProperties()
        def mockTask = new MockFor(GitCheckoutTask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('gitCheckout')
        mockTask.use {
            task.execute()
        }
    }

    @Test
    void shouldExecuteGitUpdateSHATask(){
        setupTaskProperties()
        def mockTask = new MockFor(GitUpdateSHATask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('gitUpdateSHA')
        mockTask.use {
            task.execute()
        }
    }

    @Test
    void shouldExecuteGitCommitAndPushTask(){
        setupTaskProperties()
        def mockTask = new MockFor(GitCommitAndPushTask)
        mockTask.demand.execute {}
        def task = project.tasks.findByName('gitCommitAndPush')
        mockTask.use {
            task.execute()
        }
    }

    private void setupTaskProperties(){
        project.gitconfig.userId = "testUser"
        project.gitconfig.fullRepoName = "testRepo"
        project.gitconfig.shaId = "testSHA"
    }

    def taskShouldExist(task) {
        assertThat(project.tasks.findByName(task), notNullValue())
    }

    def taskShouldDependOn(task, requiredDependency) {
        for (def dependency : project.tasks.findByName(task).dependsOn) {
            if (dependency == requiredDependency) {
                return
            } else if (dependency instanceof List) {
                for (def d : dependency) {
                    if (d == requiredDependency) {
                        return
                    }
                }
            }
        }
        fail("Task ${task} does not declare a dependency on ${requiredDependency}")
    }

}
