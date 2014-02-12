package com.gap.gradle.tasks

import static junit.framework.Assert.assertFalse
import static junit.framework.Assert.assertNotNull
import static net.sf.ezmorph.test.ArrayAssertions.assertEquals
import static org.hamcrest.MatcherAssert.assertThat
import static org.junit.internal.matchers.StringContains.containsString
import static org.junit.rules.ExpectedException.none
import static org.mockito.Matchers.anyObject
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.*
import groovy.json.JsonBuilder
import groovy.mock.interceptor.MockFor

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore;
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import com.gap.gradle.ProdDeployConfig
import com.gap.gradle.ProdDeployParameterConfig
import com.gap.gradle.jenkins.JenkinsRunner

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
		project.prodDeployParametersJsonAbsolutePath = "."
        mockJenkinsRunner = new MockFor(JenkinsRunner.class)
    }

    @Ignore
    void shouldThrowException_whenJsonPathIsNotPassed() {
		project.prodDeployParametersJsonAbsolutePath = null
        assertThrowsExceptionWithMessage("Missing required parameter: prodDeployParametersJsonAbsolutePath", {promoteToProdTask.execute()})
    }
    @Test
    void shouldThrowException_whenJenkinsServerUrlIsNotConfigured(){
        assertThrowsExceptionWithMessage("No jenkins url configured", {promoteToProdTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsUserNameIsNotConfigured(){
        project.prodJenkins.knifeServerUrl = "testserver"
        assertThrowsExceptionWithMessage("No jenkins user configured", {promoteToProdTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsApiTokenIsNotConfigured(){
        project.prodJenkins.knifeServerUrl = "jenkins"
        project.prodJenkins.knifeUser = "jenkins_user"
        assertThrowsExceptionWithMessage("No jenkins auth-token configured", {promoteToProdTask.execute()})
    }

    @Test
    void shouldThrowException_whenJenkinsJobNameIsNotConfigured(){
        project.prodJenkins.knifeServerUrl = "jenkins"
        project.prodJenkins.knifeUser = "jenkins_user"
        project.prodJenkins.knifeAuthToken = "jenkins_auth"
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
        project.prodJenkins.knifeServerUrl = "jenkins"
        project.prodJenkins.knifeUser = "jenkins_user"
        project.prodJenkins.knifeAuthToken = "jenkins_password"
        project.prodJenkins.knifeJobName = "jenkins_job"
        project.prodChef.environment = "local"
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
        ProdDeployParameterConfig config = new ProdDeployParameterConfig()
        config.sha1IdList = sha1IdList

        def jsonBuilder = new JsonBuilder(config)
        def fileWriter = new FileWriter("${ProdDeployConfig.PARAMJSON}")
        jsonBuilder.writeTo(fileWriter)
        fileWriter.close()
    }
}