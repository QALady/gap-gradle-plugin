package com.gap.gradle.tasks

import com.gap.gradle.chef.CookbookUploader
import com.gap.gradle.chef.CookbookUtil

import static junit.framework.Assert.*
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.internal.matchers.StringContains.containsString
import static org.junit.rules.ExpectedException.none
import static org.mockito.Matchers.anyObject
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.*
import groovy.mock.interceptor.MockFor

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import com.gap.gradle.jenkins.JenkinsRunner
import com.gap.pipeline.ec.CommanderClient;

class PromoteCookbookToProductionTaskTest {

    @Rule
    public final ExpectedException exception = none()

    private Task publishCookbookToChefServerTask
    private Project project
    def mockJenkinsRunner

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
		project.paramJsonPath = "src/test/groovy/com/gap/gradle/resources/"
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
    }


}