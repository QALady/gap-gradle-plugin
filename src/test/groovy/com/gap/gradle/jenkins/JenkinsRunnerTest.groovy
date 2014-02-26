package com.gap.gradle.jenkins

import static org.hamcrest.Matchers.containsString
import static org.junit.rules.ExpectedException.none
import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.*

import com.gap.gradle.jenkins.JenkinsClient
import com.gap.gradle.jenkins.JenkinsRunner
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class JenkinsRunnerTest {

    @Rule
    public final ExpectedException exception = none()

    def jenkinsClient
    def runner
    def jobName = "TagProdReady"
    def jobParams = "jobParams"

    @Before
    void SetUp() {
        jenkinsClient = mock(JenkinsClient)
        when(jenkinsClient.startJobWithParams(eq(jobName), eq(jobParams))).thenReturn(204)
		when(jenkinsClient.startJob(eq(jobName))).thenReturn(200)
        when(jenkinsClient.isFinished(eq(jobName), eq(204))).thenReturn(true)
		when(jenkinsClient.isFinished(eq(jobName), eq(200))).thenReturn(true)
        when(jenkinsClient.isSuccessful(eq(jobName), eq(204))).thenReturn(true)
		when(jenkinsClient.isSuccessful(eq(jobName), eq(200))).thenReturn(true)
        when(jenkinsClient.getJobUrl(jobName, 204)).thenReturn("http://jenkinsserver/unittest/204")
        runner = new JenkinsRunner(jenkinsClient)
    }

    @Test
    void shouldStartJenkinsJobWithParams(){
        runner.runJob(jobName, jobParams)
        verify(jenkinsClient).startJobWithParams(jobName, jobParams)
    }
	
    @Test
    void shouldReturnSuccessfullyWhenJobSuccessfullyCompleted(){
        runner.runJob(jobName, jobParams)
        verify(jenkinsClient).isFinished(jobName, 204)
        verify(jenkinsClient).isSuccessful(jobName, 204)
    }

	@Test
	void shouldStartJenkinsJobWithNoParams() {
		runner.runJob(jobName)
		verify(jenkinsClient).startJob(jobName)
		verify(jenkinsClient).isFinished(jobName, 200)
		verify(jenkinsClient).isSuccessful(jobName, 200)
	}

    @Test
    void shouldThrowExceptionWhenJenkinsJobFails(){
        exception.expect(JenkinsException)
        exception.expectMessage("Jenkins job failed <http://jenkinsserver/unittest/204>: Console log: oops, you broke it")
        when(jenkinsClient.isSuccessful(jobName, 204)).thenReturn(false)
        when(jenkinsClient.getConsole(jobName, 204)).thenReturn("oops, you broke it")
        runner.runJob(jobName, jobParams)
    }

    @Test
    void shouldPollForTheJobToBeFinished(){
        runner = new JenkinsRunner(jenkinsClient, 10)
        when(jenkinsClient.isFinished(eq(jobName), eq(204))).thenReturn(false).thenReturn(false).thenReturn(true)
        runner.runJob(jobName, jobParams)
        verify(jenkinsClient, times(3)).isFinished(jobName, 204)
        verify(jenkinsClient).isSuccessful(jobName, 204)
    }

    @Test
    void shouldTimeoutAndThrowException_whenJobTakesALongTimeToComplete (){
        exception.expect(JenkinsException)
        exception.expectMessage(containsString("Timed out after 50 ms waiting for job to finish <http://jenkinsserver/unittest/204>"))
        runner = new JenkinsRunner(jenkinsClient, 10, 50)
        when(jenkinsClient.isFinished(eq(jobName), eq(204))).thenReturn(false)
        runner.runJob(jobName, jobParams)
    }
}
