package com.gap.gradle.plugins.cookbook

import com.gap.gradle.git.GitClient
import com.gap.gradle.tasks.UpdateCookbookSHATask;

import groovy.mock.interceptor.MockFor

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static junit.framework.Assert.assertFalse
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.internal.matchers.StringContains.containsString
import static helpers.Assert.assertThrowsExceptionWithMessage

class UpdateCookbookSHATaskTest {

    private Project project
    Task updateBerksfileTask
    def mockGitClient
    private Log log = LogFactory.getLog(UpdateCookbookSHATask)

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapchef'
        updateBerksfileTask = project.tasks.findByName('promoteCookbookBerksfile')
        mockGitClient = new MockFor(GitClient)
    }

    void setUpProperties(){
        project.gitconfig.userId = 'testUser'
        project.gitconfig.shaId = 'testSHA'
        project.gitconfig.fullRepoName = 'test/repo'
    }

    @Test
    void taskShouldSuccessfullyExecute_whenConfigIsValid(){
        setUpProperties()
        mockGitClient.demand.checkout(1){ 0 }
        mockGitClient.demand.updateBerksfile(1){ 0 }
        mockGitClient.demand.commitAndPush(1){ 0 }
        mockGitClient.use{
            updateBerksfileTask.execute()
        }
    }

    @Test
    void taskShouldThrowException_whenConfigFullRepoNameIsInvalid(){
        setUpProperties()
        try{
            project.gitconfig.fullRepoName = 'testrepo'
            mockGitClient.demand.checkout(1){ -1 }
            mockGitClient.demand.updateBerksfile(1){ -1 }
            mockGitClient.demand.commitAndPush(1){ -1 }
            mockGitClient.use{
                updateBerksfileTask.execute()
            }
        } catch (Exception e){
            assertThat(e.dump(), containsString("The fullRepoName must have the " +
                    "following format: 'organization/repoName'"))
        }
    }

    @Test
    void taskShouldThrowException_whenConfigIsNotSet(){
        try{
            mockGitClient.demand.checkout(1){ -1 }
            mockGitClient.demand.updateBerksfile(1){ -1 }
            mockGitClient.demand.commitAndPush(1){ -1 }
            mockGitClient.use{
                updateBerksfileTask.execute()
            }
        } catch (Exception e){
            assertThat(e.dump(), containsString("Missing required parameter: 'gitconfig.fullRepoName'"))
        }
    }

    @Test
    void shouldThrowException_whenUserIdIsMissing(){
        project.gitconfig.shaId = 'testSHA'
        project.gitconfig.fullRepoName = 'test/repo'
        assertThrowsExceptionWithMessage("Missing required parameter: 'gitconfig.userId'", {updateBerksfileTask.execute()})
    }

    @Test
    void shouldThrowException_whenSHAIdIsMissing(){
        project.gitconfig.userId = 'testUser'
        project.gitconfig.fullRepoName = 'test/repo'
        assertThrowsExceptionWithMessage("Missing required parameter: 'gitconfig.shaId'", {updateBerksfileTask.execute()})
    }

    @Test
    void shouldThrowException_whenFullRepoNameIsMissing(){
        project.gitconfig.userId = 'testUser'
        project.gitconfig.shaId = 'testSHA'
        assertThrowsExceptionWithMessage("Missing required parameter: 'gitconfig.fullRepoName'", {updateBerksfileTask.execute()})
    }

    @Test
    void shouldThrowException_whenFullRepoNameFormatIsInvalid(){
        project.gitconfig.userId = 'testUser'
        project.gitconfig.shaId = 'testSHA'
        project.gitconfig.fullRepoName = 'testrepo'
        assertThrowsExceptionWithMessage("The fullRepoName must have the " +
                "following format: 'organization/repoName'", {updateBerksfileTask.execute()})
    }
}
