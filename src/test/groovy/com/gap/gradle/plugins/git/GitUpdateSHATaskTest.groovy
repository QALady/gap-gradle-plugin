package com.gap.gradle.plugins.git

import com.gap.gradle.utils.ShellCommand

import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.Assert.assertEquals

import groovy.mock.interceptor.MockFor
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.junit.internal.matchers.StringContains.containsString

/**
 * Created by ccaceres on 2/5/14.
 */
class GitUpdateSHATaskTest {
    Project project
    Task gitUpdateSHATask
    private Log log = LogFactory.getLog(GitUpdateSHATaskTest)
    def mockGitCheckoutTask

    @Before
    void setUp(){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapgit'
        project.gitconfig.fullRepoName = 'watchmen/gitTest'
        project.gitconfig.shaId = 'abcd123'
        gitUpdateSHATask = project.tasks.findByName('gitUpdateSHA')
        mockGitCheckoutTask = new MockFor(GitCheckoutTask)
    }

    @Test
    void shouldThrowException_whenCookbookIsNotConfigured(){
        try {
            project.gitconfig.fullRepoName = null
            gitUpdateSHATask.execute()
        } catch (Exception e) {
            assertThat(e.dump(), containsString('There is no fullRepoName defined'))
        }
    }

    @Test
    void shouldThrowException_whenSHAIdIsNotConfigured(){
        try {
            project.gitconfig.shaId = null
            gitUpdateSHATask.execute()
        } catch (Exception e) {
            assertThat(e.dump(), containsString('There is no SHA Id defined'))
        }
    }

    @Test
    void shouldSuccessfullyUpdateSHA(){
        try{
            new GitUpdateSHATask(project).execute() // TODO: use Task instead of new instance
        } catch (FileNotFoundException e){
            log.error(e.printStackTrace())
        }
    }
}