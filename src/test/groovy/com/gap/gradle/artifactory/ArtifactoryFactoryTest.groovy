package com.gap.gradle.artifactory


import org.junit.Test
import org.junit.Before
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import static groovyx.net.http.ContentType.ANY
import static groovyx.net.http.ContentType.JSON
import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

import org.jfrog.artifactory.client.impl.UploadableArtifactImpl
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.UploadableArtifact
import org.jfrog.artifactory.client.impl.ArtifactoryImpl
import org.jfrog.artifactory.client.UploadableArtifact

class ArtifactoryFactoryTest {

  def factory

  @Before
  void SetUp() {
    factory = new ArtifactoryFactory()
  }

  @Test
  void shouldCreateUploadableArtifactWithCorrectValues() {
    def resourceFile = this.getClass().getClassLoader().getResource("testrpm.rpm");
    def artifactFile = new File(resourceFile.getPath())
    def artifactoryMock = mock(ArtifactoryImpl)
    def uploadableArtifact = factory.createUploadableArtifact("repo", "artifactPath", artifactFile, artifactoryMock)
    assertThat(uploadableArtifact, instanceOf(UploadableArtifact.class))
  }
}
