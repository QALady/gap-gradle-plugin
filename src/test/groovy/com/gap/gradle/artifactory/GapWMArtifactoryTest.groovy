package com.gap.gradle.artifactory

import org.junit.Test
import static helpers.CustomMatchers.sameString
import static org.mockito.Mockito.*

import groovy.mock.interceptor.MockFor
import com.gap.pipeline.ec.CommanderClient
import org.junit.Before
import org.junit.Test


import org.jfrog.artifactory.client.ArtifactoryClient
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.impl.ArtifactoryImpl
import org.jfrog.artifactory.client.impl.UploadableArtifactImpl
import org.jfrog.artifactory.client.UploadableArtifact
import org.jfrog.artifactory.client.model.impl.FileImpl
import com.gap.gradle.artifactory.ArtifactoryFactory

class GapWMArtifactoryTest {

  GapWMArtifactory wmArtifactory
  def ecClientMock
  def factoryMock
  def rpmFilePath = "testrpm.rpm"
  def rpmPath = "watchmen/devel"
  def rpmFile

  @Before
  void SetUp() {
    def test = this.getClass().getClassLoader().getResource(rpmFilePath);
    rpmFile = new File(test.getPath())
    ecClientMock = mock(CommanderClient)
    factoryMock = mock(ArtifactoryFactory)
    when(ecClientMock.getArtifactoryUserName()).thenReturn("user")
    when(ecClientMock.getArtifactoryPassword()).thenReturn("password")
    wmArtifactory = new GapWMArtifactory(ecClientMock, factoryMock)
  }

  @Test
  void uploadRpm_should_call_doUpload(){

    def uploadableArtifactMock = mock(UploadableArtifact)
    org.jfrog.artifactory.client.model.File responseMock = mock(FileImpl)
    when(responseMock.getDownloadUri()).thenReturn("response.rpm")
    when(uploadableArtifactMock.doUpload()).thenReturn(responseMock)
    when(factoryMock.createUploadableArtifact(any(), any(), any(), any())).thenReturn(uploadableArtifactMock)

    assert "response.rpm", wmArtifactory.uploadRpm(rpmPath, rpmFilePath)
  }

}
