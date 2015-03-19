package com.gap.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import com.gap.gradle.artifactory.GapWMArtifactory
import com.gap.pipeline.ec.CommanderClient

class UploadRpm extends DefaultTask {

  def String sourcePath
  CommanderClient ecClient
  GapWMArtifactory gapArtifactory

  @TaskAction
  def run() {
    def String repoName
    def String channel = "devel"
    ecClient = new CommanderClient()
    gapArtifactory = new GapWMArtifactory(ecClient)
    File file = new File(sourcePath)
    def uploadedArtifatUrl = gapArtifactory.uploadRpm("${repoName}/${channel}", sourcePath)
    ecClient.addLinkToUrl(file.getName(), uploadedArtifatUrl)
  }
}
