package com.gap.gradle.artifactory

import static groovyx.net.http.ContentType.JSON

import groovy.json.JsonBuilder
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.gradle.api.artifacts.PublishException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import org.jfrog.artifactory.client.ArtifactoryClient
import org.jfrog.artifactory.client.Artifactory
import org.jfrog.artifactory.client.UploadableArtifact
import com.gap.pipeline.ec.CommanderClient

class GapWMArtifactory {

  private static final Logger logger = LoggerFactory.getLogger(GapWMArtifactory)

  private static final ARTIFACTORY_URL = "http://artifactory.gapinc.dev/artifactory/"
  private static final YUM_NON_PROD_REPO = "yum-non-prod"

  final String userName
  final String password
  Artifactory artifactory
  CommanderClient ecClient
  ArtifactoryFactory factory

  GapWMArtifactory(ecClient = new CommanderClient(), factory = new ArtifactoryFactory()) {
    this.ecClient = ecClient
    this.factory = factory
    userName = ecClient.getArtifactoryUserName()
    password = ecClient.getArtifactoryPassword()
    artifactory = ArtifactoryClient.create(ARTIFACTORY_URL, userName, password)
  }

  def uploadRpm(String rpmPath, String rpmFilePath) {
    File rpmFile = new File(rpmFilePath)
    rpmPath += "/${rpmFile.getName()}"
    UploadableArtifact rpmArtifact = factory.createUploadableArtifact(YUM_NON_PROD_REPO, rpmPath, rpmFile, artifactory)
    org.jfrog.artifactory.client.model.File uploadedArtifact = rpmArtifact.doUpload()
    uploadedArtifact.getDownloadUri();
  }
}
