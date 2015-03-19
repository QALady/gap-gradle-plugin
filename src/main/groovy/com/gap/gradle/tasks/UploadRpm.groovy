package com.gap.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.gap.gradle.artifactory.GapWMArtifactory
import com.gap.pipeline.ec.CommanderClient

class UploadRpm extends DefaultTask {

  def String channel = "devel"
  def String sourcePath
  def String repoName
  CommanderClient ecClient
  GapWMArtifactory gapArtifactory

  @TaskAction
  def run() {
    ecClient = new CommanderClient()
    gapArtifactory = new GapWMArtifactory(ecClient)
    File file = new File(sourcePath)
    def uploadedArtifatUrl = gapArtifactory.uploadRpm("${repoName}/${channel}", sourcePath)
    ecClient.addLinkToUrl(file.getName(), uploadedArtifatUrl)
  }
}
