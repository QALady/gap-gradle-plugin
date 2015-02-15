package com.gap.gradle.artifactory

import org.jfrog.artifactory.client.impl.UploadableArtifactImpl
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.UploadableArtifact
import com.gap.gradle.utils.Utilities

class ArtifactoryFactory {
  UploadableArtifact createUploadableArtifact(String repo, String artifactPath, File artifactFile, Artifactory artifactory) {
    String sha1String  = Utilities.calculateSha1(artifactFile)
    UploadableArtifactImpl rpmArtifact = new UploadableArtifactImpl(repo, artifactPath, artifactFile.newInputStream(), artifactory)
    rpmArtifact.bySha1Checksum(sha1String)
  }
}
