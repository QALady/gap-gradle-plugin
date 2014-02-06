package com.gap.gradle.plugins.git

import com.gap.gradle.utils.ShellCommand

import static junit.framework.Assert.assertEquals

import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.internal.matchers.StringContains.containsString

/**
 * Created by ccaceres on 2/4/14.
 */
class GitCheckoutTaskTest {

    private Project project
    Task gitCheckoutTask
    def mockShellCommand
    def fullRepoName

    @Before
    void setUp(){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapgit'
        project.gitconfig.fullRepoName = 'watchmen/gitTest'
        gitCheckoutTask = project.tasks.findByName('gitCheckout')
        mockShellCommand = new MockFor(ShellCommand)
    }

    @Test
    void shouldThrowException_whenCookbookIsNotConfigured(){
        try {
            project.gitconfig.fullRepoName = null
            gitCheckoutTask.execute()
        } catch (Exception e) {
            assertThat(e.dump(), containsString('There is no fullRepoName defined'))
        }
    }

    @Test
    void shouldSuccessfullyExecute(){
        mockShellCommand.demand.execute { 0 }
        mockShellCommand.use {
           assertEquals(0, new GitCheckoutTask(project).execute()) //TODO: Use task instead of new instance
        }
    }
}
