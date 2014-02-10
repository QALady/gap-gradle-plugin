package com.gap.gradle.jenkins
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertEquals

import groovy.mock.interceptor.MockFor
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.junit.Before
import org.junit.Test

class JenkinsClientTest {

    def client
    def mockHttpBuilder
    def mockRequestDelegate = [response: [:], uri: [:], headers: [:], requestContentType: [:], body: [:]]
    def mockResponse = [resp: [:]]

    @Before
    void setUp() {
        mockHttpBuilder = new MockFor(HTTPBuilder)
        client = new JenkinsClient("serverUrl", "junitUser", "junitApiToken")
    }

    @Test
    void startJob_shouldTriggerNewJobInJenkinsServer (){
        mockHttpBuilder.demand.request {method, contentType, body -> }
        mockHttpBuilder.demand.request {Method method, Closure body ->
            body.delegate = mockRequestDelegate
            body.call()
            assertEquals(POST, method)
            assertEquals("/job/jenkins_job_name/build", mockRequestDelegate.uri.path.toString() )
            assertAuthorizationHeader(mockRequestDelegate)
        }
        mockHttpBuilder.use {
            client.startJob("jenkins_job_name")
        }
    }

    @Test(expected = JenkinsException)
    void startJob_shouldThrowException_whenFailedToTriggerNewJobInJenkins (){
        mockHttpBuilder.demand.request {method, contentType, body -> }
        mockHttpBuilder.demand.request {Method method, Closure body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.failure(mockResponse)
        }

        mockHttpBuilder.use {
            client.startJob("jenkins_job_name")
        }
    }

    @Test
    void startJob_shouldReturnNextBuildNumberFromJenkins_whenJobSuccessfullyKickedOff() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            assertThat(method, equalTo(GET))
            assertThat(mockRequestDelegate.uri.path.toString(), equalTo("/job/jenkins_job_name/api/json"))

            mockRequestDelegate.response.success(mockResponse, [nextBuildNumber: 203])
        }

        mockHttpBuilder.demand.request {method, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.success()
        }
        mockHttpBuilder.use{
          assertThat(client.startJob("jenkins_job_name"), equalTo(203))
        }
    }

    @Test(expected = JenkinsException)
    void startJob_shouldThrowException_whenFailedToGetNextBuildNumber (){
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.failure(mockResponse)
        }

        mockHttpBuilder.use {
            client.startJob("jenkins_job_name")
        }
    }


    @Test
    void isFinished_shouldInvokeJenkinsApi() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            assertThat(method, equalTo(GET))
            assertEquals(JSON, contentType)
            assertThat(mockRequestDelegate.uri.path.toString(), equalTo("/job/jenkins_job_name/203/api/json"))
        }

        mockHttpBuilder.use {
            client.isFinished("jenkins_job_name", 203)
        }
    }

    @Test
    void isFinished_shouldReturnTrue_WhenJobIsComplete() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.success(mockResponse, [building: false])
        }

        mockHttpBuilder.use {
            assertEquals(true, client.isFinished("jenkins_job_name", 203))
        }
    }

    @Test
    void isFinished_shouldReturnFalse_WhenJobIsInProgress() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.success(mockResponse, [building: true])
        }

        mockHttpBuilder.use {
            assertEquals(false, client.isFinished("jenkins_job_name", 203))
        }
    }

    @Test
    void isFinished_shouldReturnFalse_WhenJobCannotBeFound() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.failure([statusLine: [statusCode: 404]])
        }

        mockHttpBuilder.use {
            assertEquals(false, client.isFinished("jenkins_job_name", 203))
        }
    }

    @Test(expected = JenkinsException)
    void isFinished_shouldThrowException_whenRequestToJenkinsApiFails() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.failure([statusLine: [statusCode: 500]])
        }

        mockHttpBuilder.use {
            client.isFinished("jenkins_job_name", 203)
        }
    }

    private void assertAuthorizationHeader(requestDelegate) {
        assertEquals("Basic  anVuaXRVc2VyOmp1bml0QXBpVG9rZW4=", requestDelegate.headers.Authorization.toString())
    }
    @Test
    void isSuccessful_shouldReturnTrue_whenJenkinsJobIsSuccessful() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.success(mockResponse, [building: false, result: 'SUCCESS'])
        }

        mockHttpBuilder.use {
            assertTrue(client.isSuccessful("job name", 203))
        }
    }

    @Test
    void isSuccessful_shouldReturnFalse_whenJenkinsJobFails() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.success(mockResponse, [building: false, result: 'FAILURE'])
        }

        mockHttpBuilder.use {
            assertFalse(client.isSuccessful("job name", 203))
        }
    }

    @Test
    void getConsole_shouldInvokeJenkinsApi() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            assertEquals(GET, method)
            assertEquals(TEXT, contentType)
            assertThat(mockRequestDelegate.uri.path.toString(), equalTo("/job/jenkins_job_name/203/logText/progressiveText"))
        }

        mockHttpBuilder.use {
            client.getConsole("jenkins_job_name", 203)
        }
    }

    @Test
    void getConsole_shouldReturnConsoleOutput_whenSuccessful() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.success(mockResponse, [text: 'this is jenkins console output'])
        }

        mockHttpBuilder.use {
            assertEquals ('this is jenkins console output', client.getConsole("jenkins_job_name", 203))
        }
    }

    @Test(expected = JenkinsException)
    void getConsole_shouldThrowJenkinsExceptoin_whenFailed() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.failure(mockResponse)
        }

        mockHttpBuilder.use {
            assertEquals ('this is jenkins console output', client.getConsole("jenkins_job_name", 203))
        }
    }

    @Test
    void getJobUrl_shouldReturnTheFullJobUrl(){
        assertEquals("serverUrl/job/jenkins_job_name/203", client.getJobUrl("jenkins_job_name", 203))
    }

    @Test
    void startJobWithParams_shouldTriggerNewJobInJenkinsServer (){
        mockHttpBuilder.demand.request {method, contentType, body -> }
        mockHttpBuilder.demand.request {Method method, Closure body ->
            body.delegate = mockRequestDelegate
            body.call()
            assertEquals(POST, method)
            assertEquals("/job/jenkins_job_name/buildWithParameters", mockRequestDelegate.uri.path.toString())
            assertEquals("job_params", mockRequestDelegate.body)
            assertEquals(URLENC, mockRequestDelegate.requestContentType)
            assertAuthorizationHeader(mockRequestDelegate)
        }
        mockHttpBuilder.use {
            client.startJobWithParams("jenkins_job_name", "job_params")
        }
    }

    @Test(expected = JenkinsException)
    void startJobWithParams_shouldThrowException_whenFailedToTriggerNewJobInJenkins (){
        mockHttpBuilder.demand.request {method, contentType, body -> }
        mockHttpBuilder.demand.request {Method method, Closure body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.failure(mockResponse)
        }

        mockHttpBuilder.use {
            client.startJobWithParams("jenkins_job_name", "job_params")
        }
    }

    @Test
    void startJobWithParams_shouldReturnNextBuildNumberFromJenkins_whenJobSuccessfullyKickedOff() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            assertThat(method, equalTo(GET))
            assertThat(mockRequestDelegate.uri.path.toString(), equalTo("/job/jenkins_job_name/api/json"))

            mockRequestDelegate.response.success(mockResponse, [nextBuildNumber: 203])
        }

        mockHttpBuilder.demand.request {method, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.success()
        }
        mockHttpBuilder.use{
            assertThat(client.startJobWithParams("jenkins_job_name", "job_params"), equalTo(203))
        }
    }
}
