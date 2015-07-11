package com.gap.gradle.tasks

import com.gap.gradle.jenkins.JenkinsRunner
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static junit.framework.Assert.assertEquals
import static org.junit.rules.ExpectedException.none

class PromoteCookbookToProductionTaskTest {

    @Rule
    public final ExpectedException exception = none()

    private Task publishCookbookToChefServerTask
    private Project project
    def mockJenkinsRunner

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
		project.metaClass.paramJsonPath = "src/test/groovy/com/gap/gradle/resources/"
		project.apply plugin: 'gapproddeploy'
        publishCookbookToChefServerTask = project.tasks.findByName('promoteCookbookToProdChefServer')
        mockJenkinsRunner = new MockFor(JenkinsRunner.class)
    }

    @Test
    void shouldTriggerPromoteChefObjectsJob_whenAllParametersArePassed(){
        setupTaskProperties()

        mockJenkinsRunner.demand.runJob { jobName ->
            assertEquals("cookbook-test-app-local", jobName)
        }

        mockJenkinsRunner.use {
			publishCookbookToChefServerTask.execute()
        }
    }

    private void setupTaskProperties() {
        project.jenkins.cookbookServerUrl = "jenkins"
        project.jenkins.cookbookUser = "jenkins_user"
        project.jenkins.cookbookAuthToken = "jenkins_password"
        project.chef.environment = "local"
		project.metaClass.cookbookJenkinsApiAuthToken = "testtoken"
    }


}