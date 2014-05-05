package com.gap.gradle.chef

import com.gap.gradle.jenkins.JenkinsClient
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static org.hamcrest.Matchers.containsString
import static org.junit.rules.ExpectedException.none
import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.*

class CookbookUploaderTest {

    @Rule
    public final ExpectedException exception = none()

    JenkinsClient jenkinsClient
    CookbookUtil  cookbookUtil
    def uploader

    @Before
    void SetUp() {
        jenkinsClient = mock(JenkinsClient)
        cookbookUtil = mock(CookbookUtil)
        when(jenkinsClient.startJob(anyString())).thenReturn(204)
        when(jenkinsClient.isFinished(anyString(), eq(204))).thenReturn(true)
        when(jenkinsClient.isSuccessful(anyString(), eq(204))).thenReturn(true)
        when(jenkinsClient.getJobUrl("cookbook-mycookbook-tdev", 204)).thenReturn("http://jenkinsserver/unittest/204")
        uploader = new CookbookUploader(jenkinsClient)
    }

    @Test
    void shouldStartJenkinsJobForUploadingCookbook(){
        uploader.upload("tdev", [name: 'mycookbook' , version: '1.1.13'])
        verify(jenkinsClient).startJob("cookbook-mycookbook-tdev")
    }

    @Test
    void shouldReturnSuccessfullyWhenJobSuccessfullyCompleted(){
        uploader.upload("tdev", [name: 'mycookbook', version: '1.1.13'])
        verify(jenkinsClient).isFinished('cookbook-mycookbook-tdev', 204)
        verify(jenkinsClient).isSuccessful('cookbook-mycookbook-tdev', 204)
    }

    @Test
    void shouldThrowExceptionWhenJenkinsJobFails(){
        exception.expect(ChefException)
        exception.expectMessage("Jenkins job failed <http://jenkinsserver/unittest/204>: Console log: oops, you broke it")
        when(jenkinsClient.isSuccessful('cookbook-mycookbook-tdev', 204)).thenReturn(false)
        when(jenkinsClient.getConsole('cookbook-mycookbook-tdev', 204)).thenReturn("oops, you broke it")
        uploader.upload("tdev", [name: 'mycookbook', version: '1.1.13'])
    }

    @Test
    void shouldPollForTheJobToBeFinished(){
        uploader = new CookbookUploader(jenkinsClient, 10)
        when(jenkinsClient.isFinished(anyString(), eq(204))).thenReturn(false).thenReturn(false).thenReturn(true)
        uploader.upload( "tdev", [name: 'mycookbook', version: '1.1.13'])
        verify(jenkinsClient, times(3)).isFinished('cookbook-mycookbook-tdev', 204)
        verify(jenkinsClient).isSuccessful('cookbook-mycookbook-tdev', 204)
    }

    @Test
    void shouldReturnSuccessfullyIfTheJenkinsJobIsNotCompleteButTheCookbookHasBeenUploaded(){
        def cookbookMetaData = [name: 'mycookbook', version: '1.1.13']
        uploader = new CookbookUploader(jenkinsClient, 10, 60000, cookbookUtil)
        when(jenkinsClient.isFinished(anyString(), eq(204))).thenReturn(false)
        when(jenkinsClient.isSuccessful('cookbook-mycookbook-tdev', 204)).thenReturn(false)
        when(cookbookUtil.doesCookbookExist(cookbookMetaData)).thenReturn(false).thenReturn(true)
        uploader.upload( "tdev", cookbookMetaData)
        verify(cookbookUtil, times(3)).doesCookbookExist(cookbookMetaData)
    }

    @Test
    void shouldTimeoutAndThrowException_whenJobTakesALongTimeToComplete (){
        exception.expect(ChefException)
        exception.expectMessage(containsString("Timed out after 50 ms waiting for job to finish <http://jenkinsserver/unittest/204>"))
        uploader = new CookbookUploader(jenkinsClient, 10, 50)
        when(jenkinsClient.isFinished(anyString(), eq(204))).thenReturn(false)
        uploader.upload( "tdev", [name: 'mycookbook', version: '1.1.13'])
    }
}
