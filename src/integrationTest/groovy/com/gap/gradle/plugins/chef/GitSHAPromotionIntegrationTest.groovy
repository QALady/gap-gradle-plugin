package com.gap.gradle.plugins.chef
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

class GitSHAPromotionIntegrationTest {
    private Project project
    Task updateBerksfileTask
    def shaId
    def cookbook
    private Log log = LogFactory.getLog(GitSHAPromotionIntegrationTest)
    private Random random = new Random()

    @Before
    void setUp(){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapchef'
        updateBerksfileTask = project.tasks.findByName('promoteCookbookBerksfile')
        setUpProperties()
        cookbook = project.gitconfig.fullRepoName.tokenize('/')[1]
        deleteRepo()
    }

    def setUpProperties(){
        project.gitconfig.fullRepoName = 'watchmen/gitTest'
        project.gitconfig.shaId = random.nextInt(Integer.MAX_VALUE)
        project.gitconfig.userId = 'Ca9s7i9'
    }

    @Test
    void updateShouldSucceed_whenParametersAreValid(){
        try {
            updateBerksfileTask.execute()
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
        if (!System.properties['os.name'].toString().toLowerCase().contains('windows')){
            new ShellCommand().execute('rm -rf ' + cookbook)
        }
    }
}
