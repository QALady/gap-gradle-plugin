package com.gap.gradle.plugins.chef
import com.gap.gradle.utils.ShellCommand
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
    private Random random = new Random()

    @Before
    void setUp(){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapchef'
        updateBerksfileTask = project.tasks.findByName('promoteCookbookBerksfile')
        setUpProperties()
        cookbook = project.git.fullRepoName.tokenize('/')[1]
        deleteRepo()
    }

    def setUpProperties(){
        project.git.fullRepoName = 'watchmen/gitTest'
        project.git.sha1Id = random.nextInt(Integer.MAX_VALUE)
        project.git.userId = 'Ca9s7i9'
    }

    @Test
    void updateShouldSucceed_whenParametersAreValid(){
        updateBerksfileTask.execute()
    }

    @After
    void cleanUp(){
        cookbook = project.git.fullRepoName.split('/')[1]
        deleteRepo()
    }

    void deleteRepo(){
        if (!System.properties['os.name'].toString().toLowerCase().contains('windows')){
            new ShellCommand().execute('rm -rf ' + cookbook)
        }
    }
}
