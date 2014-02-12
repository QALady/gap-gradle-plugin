package com.gap.gradle.tasks

import groovy.json.JsonBuilder
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.api.Task
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertNotNull
import static org.junit.rules.ExpectedException.none
import static org.mockito.Matchers.anyObject
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.*


import org.gradle.testfixtures.ProjectBuilder

import static junit.framework.Assert.assertFalse
import static org.junit.internal.matchers.StringContains.containsString
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals
import static org.hamcrest.MatcherAssert.assertThat

import com.gap.gradle.jenkins.JenkinsClient
import com.gap.gradle.jenkins.JenkinsRunner
import com.gap.gradle.ProdDeployConfig

class PromoteToProductionTaskTest {

    @Rule
    public final ExpectedException exception = none()

    private Task promoteToProdTask
    private Project project
    def mockJenkinsRunner
    def sha1IdList = ["1234", "5678"]

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapproddeploy'
        promoteToProdTask = project.tasks.findByName('promoteToProduction')

        mockJenkinsRunner = new MockFor(JenkinsRunner.class)
    }

    //@Test
    void shouldThrowException_whenJsonPathIsNotPassed() {
        assertThrowsExceptionWithMessage("Missing required parameter: prodDeployParametersJsonAbsolutePath", {promoteToProdTask.execute()})
    }
    @Test
    void shouldThrowException_whenJenkinsServerUrlIsNotConfigured(){
        assertThrowsExceptionWithMessage("No jenkins url configured", {promoteToProdTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsUserNameIsNotConfigured(){
        project.jenkins.knifeServerUrl = "testserver"
        assertThrowsExceptionWithMessage("No jenkins user configured", {promoteToProdTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsApiTokenIsNotConfigured(){
        project.jenkins.knifeServerUrl = "jenkins"
        project.jenkins.knifeUser = "jenkins_user"
        assertThrowsExceptionWithMessage("No jenkins auth-token configured", {promoteToProdTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsJobNameIsNotConfigured(){
        project.jenkins.knifeServerUrl = "jenkins"
        project.jenkins.knifeUser = "jenkins_user"
        project.jenkins.knifeAuthToken = "jenkins_auth"
        assertThrowsExceptionWithMessage("No jenkins jobName configured", {promoteToProdTask.execute()})
    }

    @Test
    void shouldTriggerPromoteChefObjectsJob_whenAllParametersArePassed(){
        setupTaskProperties()
        createProdDeployConfigFile()

        mockJenkinsRunner.demand.runJob (2) { jobName, params ->
            assertEquals("jenkins_job", jobName)
            assertNotNull(params)
        }

        mockJenkinsRunner.use {
            promoteToProdTask.execute()
        }
    }

    //@Test
    void shouldThrowException_whenJenkinsJobIsFailed(){
        setupTaskProperties()
        createProdDeployConfigFile()

        mockJenkinsRunner.demand.runJob(2) {jobName, params ->
            throw new Exception()
        }

        mockJenkinsRunner.use {
            promoteToProdTask.execute()
        }
    }

//    @Test
    void shouldNotTriggerUpload_whenCookbookVersionAlreadyExistsInChef(){
//        setupTaskProperties()
//
//        mockCookbookUtil.demand.doesCookbookExist { return true }
//
//        mockCookbookUploader.ignore.upload {}
//
//        mockCookbookUploader.use{
//            mockCookbookUtil.use{
//                publishCookbookTask.execute()
//            }
//        }
    }

    private void setupTaskProperties() {
        project.jenkins.knifeServerUrl = "jenkins"
        project.jenkins.knifeUser = "jenkins_user"
        project.jenkins.knifeAuthToken = "jenkins_password"
        project.jenkins.knifeJobName = "jenkins_job"
        project.chef.environment = "local"
    }

    void assertThrowsExceptionWithMessage(expectedMessage, Closure closure){
        try{
            closure()
            assertFalse("Expected exception with message '${expectedMessage} but got none", true)
        }
        catch(Exception ex){
            assertThat(ex.dump(), containsString(expectedMessage))
        }
    }

    void createProdDeployConfigFile(){
        ProdDeployConfig config = new ProdDeployConfig()
        config.sha1IdList = sha1IdList

        def jsonBuilder = new JsonBuilder(config)
        def fileWriter = new FileWriter("${ProdDeployConfig.PARAMJSON}")
        jsonBuilder.writeTo(fileWriter)
        fileWriter.close()
    }
}