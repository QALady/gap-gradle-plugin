package com.gap.gradle.chef
import static org.hamcrest.Matchers.containsString
import static org.junit.rules.ExpectedException.none
import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.*

import com.gap.gradle.jenkins.JenkinsClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

class CookbooksUploaderTest {

    @Rule
    public final ExpectedException exception = none()

    def jenkinsClient
    def uploader

    @Before
    void SetUp() {
        jenkinsClient = mock(JenkinsClient)
        when(jenkinsClient.startJob(anyString())).thenReturn(204)
        when(jenkinsClient.isFinished(anyString(), eq(204))).thenReturn(true)
        when(jenkinsClient.isSuccessful(anyString(), eq(204))).thenReturn(true)
        when(jenkinsClient.getJobUrl("cookbook-mycookbook-tdev", 204)).thenReturn("http://jenkinsserver/unittest/204")
        uploader = new CookbookUploader(jenkinsClient)
    }

    @Test
    void shouldStartJenkinsJobForUploadingCookbook(){
        uploader.upload("mycookbook", "tdev")
        verify(jenkinsClient).startJob("cookbook-mycookbook-tdev")
    }

    @Test
    void shouldReturnSuccessfullyWhenJobSuccessfullyCompleted(){
        uploader.upload("mycookbook", "tdev")
        verify(jenkinsClient).isFinished('cookbook-mycookbook-tdev', 204)
        verify(jenkinsClient).isSuccessful('cookbook-mycookbook-tdev', 204)
    }

    @Test
    void shouldThrowExceptionWhenJenkinsJobFails(){
        exception.expect(ChefException)
        exception.expectMessage(containsString("Jenkins cookbook job cookbook-mycookbook-tdev #204 failed <http://jenkinsserver/unittest/204>"))
        when(jenkinsClient.isSuccessful('cookbook-mycookbook-tdev', 204)).thenReturn(false)
        uploader.upload("mycookbook", "tdev")
    }

    @Test
    void shouldPollForTheJobToBeFinished(){
        uploader = new CookbookUploader(jenkinsClient, 10)
        when(jenkinsClient.isFinished(anyString(), eq(204))).thenReturn(false).thenReturn(false).thenReturn(true)
        uploader.upload("mycookbook", "tdev")
        verify(jenkinsClient, times(3)).isFinished('cookbook-mycookbook-tdev', 204)
        verify(jenkinsClient).isSuccessful('cookbook-mycookbook-tdev', 204)
    }

    @Test
    void shouldTimeoutAndThrowException_whenJobTakesALongTimeToComplete (){
        exception.expect(ChefException)
        exception.expectMessage(containsString("Timed out waiting for job to finish cookbook-mycookbook-tdev #204 <http://jenkinsserver/unittest/204>"))
        uploader = new CookbookUploader(jenkinsClient, 10, 200)
        when(jenkinsClient.isFinished(anyString(), eq(204))).thenReturn(false)
        uploader.upload("mycookbook", "tdev")
    }
}
