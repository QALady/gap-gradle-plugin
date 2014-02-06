package com.gap.gradle.plugins.git

import com.gap.gradle.utils.ShellCommand
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static junit.framework.Assert.assertEquals
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.internal.matchers.StringContains.containsString

/**
 * Created by ccaceres on 2/5/14.
 */
class GitCommitAndPushTaskTest {

    private Project project
    Task gitCommitAndPushTask
    def mockShellCommand

    @Before
    void setUp(){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapgit'
        project.gitconfig.fullRepoName = 'watchmen/gitTest'
        project.gitconfig.userId = 'ca9s7i9'
        gitCommitAndPushTask = project.tasks.findByName('gitCommitAndPush')
        mockShellCommand = new MockFor(ShellCommand)
    }

    @Test
    void shouldThrowException_whenCookbookIsNotConfigured(){
        try {
            project.gitconfig.fullRepoName = null
            gitCommitAndPushTask.execute()
        } catch (Exception e) {
            assertThat(e.dump(), containsString('There is no fullRepoName defined'))
        }
    }

    @Test
    void shouldThrowException_whenUserIdIsNotConfigured(){
        try {
            project.gitconfig.userId = null
            gitCommitAndPushTask.execute()
        } catch (Exception e) {
            assertThat(e.dump(), containsString('There is no user id defined'))
        }
    }

    @Test
    void shouldSuccessfullyExecute(){
        mockShellCommand.demand.execute(3) { 0 }
        mockShellCommand.use {
            assertEquals(0, new GitCommitAndPushTask(project).execute()) //TODO: Use task instead of new instance
        }
    }
}
