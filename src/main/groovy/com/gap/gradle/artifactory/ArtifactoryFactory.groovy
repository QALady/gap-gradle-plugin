package com.gap.gradle.artifactory

import org.jfrog.artifactory.client.impl.UploadableArtifactImpl
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.UploadableArtifact

class ArtifactoryFactory {
  UploadableArtifact createUploadableArtifact(String repo, String artifactPath, File artifactFile, Artifactory artifactory) {
    new UploadableArtifactImpl(repo, artifactPath, artifactFile.newInputStream(), artifactory)
  }
}
