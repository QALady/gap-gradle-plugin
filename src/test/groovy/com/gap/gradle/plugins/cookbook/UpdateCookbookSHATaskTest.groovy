package com.gap.gradle.plugins.cookbook
import com.gap.gradle.git.GitClient
import com.gap.gradle.tasks.UpdateCookbookSHATask
import com.gap.pipeline.ec.CommanderClient
import groovy.mock.interceptor.MockFor
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static helpers.Assert.assertThrowsExceptionWithMessage
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.containsString

class UpdateCookbookSHATaskTest {

    private Project project
    Task updateBerksfileTask
    def mockGitClient
	def mockCommanderClient
    private Log log = LogFactory.getLog(UpdateCookbookSHATask)

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.paramJsonPath = "src/test/groovy/com/gap/gradle/resources/"
        project.apply plugin: 'gapproddeploy'
        println("**************" + project.git.fullRepoName)
        updateBerksfileTask = project.tasks.findByName('promoteCookbookBerksfile')
        mockGitClient = new MockFor(GitClient)
		mockCommanderClient = new MockFor(CommanderClient.class)
    }

    void setUpProperties(){
		mockCommanderClient.demand.getUserId(1) {'testUser'}
    }

    @Test
    void taskShouldSuccessfullyExecute_whenConfigIsValid(){
        setUpProperties()
        mockGitClient.demand.checkout(1){ 0 }
        mockGitClient.demand.updateBerksfile(1){ 0 }
        mockGitClient.demand.commitAndPush(1){ 0 }
        mockGitClient.use{
			mockCommanderClient.use {
				updateBerksfileTask.execute()
			}
        }
    }

    @Test
    void taskShouldThrowException_whenConfigFullRepoNameIsInvalid(){
        setUpProperties()
        try{
            project.git.fullRepoName = 'testrepo'
            mockGitClient.demand.checkout(1){ -1 }
            mockGitClient.demand.updateBerksfile(1){ -1 }
            mockGitClient.demand.commitAndPush(1){ -1 }
            mockGitClient.use{
				mockCommanderClient.use {
					updateBerksfileTask.execute()					
				}
            }
        } catch (Exception e){
            assertThat(e.dump(), containsString("The fullRepoName must have the " +
                    "following format: 'organization/repoName'"))
        }
    }

    @Test
    void taskShouldThrowException_whenConfigIsNotSet(){
        try{
            project.git.fullRepoName = ""
            mockGitClient.demand.checkout(1){ -1 }
            mockGitClient.demand.updateBerksfile(1){ -1 }
            mockGitClient.demand.commitAndPush(1){ -1 }
            mockGitClient.use{
				mockCommanderClient.use {
					updateBerksfileTask.execute()
				}
            }
        } catch (Exception e){
            assertThat(e.dump(), containsString("Missing required parameter: 'git.fullRepoName'"))
        }
    }

    @Test
    void shouldThrowException_whenSHAIdIsMissing(){
        project.git.sha1Id = ""
        project.git.fullRepoName = 'test/repo'
        assertThrowsExceptionWithMessage("Missing required parameter: 'git.sha1Id'", {updateBerksfileTask.execute()})
    }

    @Test
    void shouldThrowException_whenFullRepoNameIsMissing(){
        project.git.fullRepoName = ""
        project.git.sha1Id = 'testSHA'
        assertThrowsExceptionWithMessage("Missing required parameter: 'git.fullRepoName'", {updateBerksfileTask.execute()})
    }

    @Test
    void shouldThrowException_whenFullRepoNameFormatIsInvalid(){
        project.git.sha1Id = 'testSHA'
        project.git.fullRepoName = 'testrepo'
        assertThrowsExceptionWithMessage("The fullRepoName must have the " +
                "following format: 'organization/repoName'", {updateBerksfileTask.execute()})
    }
}
