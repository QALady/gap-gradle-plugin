package com.gap.gradle.jenkins

import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static org.hamcrest.MatcherAssert.assertThat
import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertEquals

import groovy.mock.interceptor.MockFor
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.junit.Before
import org.junit.Test

class JenkinsClientTest {

    def client
    def mockHttpBuilder
    def mockRequestDelegate = [response: [:], uri: [:], headers: [:]]
    def mockResponse = [resp: [:]]
    @Before
    void setUp() {
        mockHttpBuilder = new MockFor(HTTPBuilder)
        client = new JenkinsClient("serverUrl", "junitUser", "junitApiToken")
    }

    @Test
    void shouldTriggerNewJobInJenkinsServer_whenJobIsKickedOff (){
        mockHttpBuilder.demand.request {method, contentType, body -> }
        mockHttpBuilder.demand.request {Method method, Closure body ->
            body.delegate = mockRequestDelegate
            body.call()
            assertEquals(POST, method)
            assertEquals("/job/jenkins_job_name/build", mockRequestDelegate.uri.path.toString() )
            assertEquals("Basic  anVuaXRVc2VyOmp1bml0QXBpVG9rZW4=",mockRequestDelegate.headers.Authorization.toString())
        }
        mockHttpBuilder.use {
            client.startJob("jenkins_job_name")
        }
    }

    @Test(expected = JenkinsException)
    void shouldThrowException_whenFailedToTriggerNewJobInJenkins (){
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
    void shouldReturnNextBuildNumberFromJenkins_whenJobSuccessfullyKickedOff() {
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            assertThat(method, equalTo(GET))
            assertThat(mockRequestDelegate.uri.path.toString(), equalTo("/job/jenkins_job_name/api/json"))
            assertThat(mockRequestDelegate.headers.Authorization.toString(), equalTo("Basic  anVuaXRVc2VyOmp1bml0QXBpVG9rZW4="))

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
    void shouldThrowException_whenFailedToGetNextBuildNumbe (){
        mockHttpBuilder.demand.request {method, contentType, body ->
            body.delegate = mockRequestDelegate
            body.call()
            mockRequestDelegate.response.failure(mockResponse)
        }

        mockHttpBuilder.use {
            client.startJob("jenkins_job_name")
        }
    }



//
//    @Test
//    void shouldGetWhetherJobIsFinished_forJobNameAndBuildNumber() {
//        assertThat(client.isFinished("job name", 203), is(false)) // job is not finished
//    }
//
//    @Test
//    void shouldGetWhetherJobIsSuccessful_forJobNameAndBuildNumber() {
//        assertThat(client.isSuccessful("job name", 203), is(true)) // job is finished and successful
//    }
//
//    @Test(expected = IllegalStateException)
//    void shouldThrowException_whenNotFinished() {
//        client.isSuccessful("job name", 203) // should throw exception because "job name"@203 is not finished
//    }
//
//    @Test
//    void shouldGetConsoleLog() {
//        assertThat(client.getConsole("job name", 203), containsString("hello world"))
//    }
}
