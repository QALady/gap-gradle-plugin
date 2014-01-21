package com.gap.gradle.chef
import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.*

import com.gap.gradle.jenkins.JenkinsClient
import org.junit.Before
import org.junit.Test

class CookbooksUploaderTest {

    def jenkinsClient
    def uploader

    @Before
    void SetUp() {
        jenkinsClient = mock(JenkinsClient)
        when(jenkinsClient.startJob(anyString())).thenReturn(204)
        when(jenkinsClient.isFinished(anyString(), eq(204))).thenReturn(true)
        when(jenkinsClient.isSuccessful(anyString(), eq(204))).thenReturn(true)
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

    @Test(expected = ChefException)
    void shouldThrowExceptionWhenJenkinsJobFails(){
        when(jenkinsClient.getJobUrl("cookbook-mycookbook-tdev", 204)).thenReturn("http://job/url")
        when(jenkinsClient.isSuccessful('cookbook-mycookbook-tdev', 204)).thenReturn(false)
        uploader.upload("mycookbook", "tdev")
    }
}
