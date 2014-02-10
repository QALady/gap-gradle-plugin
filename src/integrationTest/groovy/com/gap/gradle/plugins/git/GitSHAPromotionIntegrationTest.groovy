package com.gap.gradle.plugins.git

import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.After
import org.junit.Before
import org.junit.Test

/**
 * Created by ccaceres on 2/5/14.
 */
class GitSHAPromotionIntegrationTest {
    private Project project
    Task gitCheckoutTask
    Task gitUpdateSHATask
    Task gitCommitAndPushTask
    def shaId
    def cookbook
    private Log log = LogFactory.getLog(GitSHAPromotionIntegrationTest)
    private Random random = new Random()

    @Before
    void setUp(){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapgit'
        project.gitconfig.fullRepoName = 'watchmen/gitTest'
        project.gitconfig.shaId = random.nextInt(Integer.MAX_VALUE)
        project.gitconfig.userId = 'Ca9s7i9'
        gitCheckoutTask = project.tasks.findByName('gitCheckout')
        gitUpdateSHATask = project.tasks.findByName('gitUpdateSHA')
        gitCommitAndPushTask = project.tasks.findByName('gitCommitAndPush')
        cookbook = project.gitconfig.fullRepoName.tokenize('/')[1]
        deleteRepo()
    }

    @Test
    void shouldSucceedSHAUpdate_whenRepoAndSHAAreValid(){
        try {
            gitCheckoutTask.execute()
            gitUpdateSHATask.execute()
            gitCommitAndPushTask.execute()
        }
        catch (ShellCommandException e){
            log.error(e.printStackTrace())
        }
    }

    @After
    void cleanUp(){
        cookbook = project.gitconfig.fullRepoName.split('/')[1]
        deleteRepo()
    }

    void deleteRepo(){
        new ShellCommand().execute('rm -rf ' + cookbook)
    }
}
