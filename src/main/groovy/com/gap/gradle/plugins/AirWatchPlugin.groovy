package com.gap.gradle.plugins

import com.gap.gradle.airwatch.AirWatchClient
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolveException

class AirWatchPlugin implements Plugin<Project> {

  private Project project

  @Override
  void apply(Project project) {
    this.project = project

    createTasks()
  }

  void createTasks() {
    project.task("validateProperties") << {
      if (!project.hasProperty("target")) {
        throw new InvalidUserDataException("You need to define the target to distribute, e.g. -Ptarget=xyz. See available targets with `xcodebuild -list`.")
      }

      if (!project.hasProperty("artifactVersion")) {
        throw new InvalidUserDataException("You need to define the artifact version to distribute, e.g. -PartifactVersion=1.123.123.")
      }

      if (!project.hasProperty("awEnv")) {
        throw new InvalidUserDataException("You need to define the AirWatch environment you want to push to, e.g. -PawEnv=CN11.")
      }

      if (!project.hasProperty("aw${project.awEnv}Host") ||
          !project.hasProperty("aw${project.awEnv}User") ||
          !project.hasProperty("aw${project.awEnv}Pass")) {
        throw new InvalidUserDataException("Environment ${project.awEnv} is not defined. You should have the following entries in your gradle.properties file:\n- aw${project.awEnv}Host\n- aw${project.awEnv}User\n- aw${project.awEnv}Pass")
      }

      if (!project.hasProperty("awTenantCode")) {
        throw new InvalidUserDataException("You need to define the tenant code, e.g. -PawTenantCode=AAA111BBB222CCC333.")
      }
    }

    project.task("configureAirWatchEnvironment", dependsOn: "validateProperties") << {
      def awHost = project.get("aw${project.awEnv}Host")
      def awUser = project.get("aw${project.awEnv}User")
      def awPass = project.get("aw${project.awEnv}Pass")

      project.set("awClient", new AirWatchClient(awHost, awUser, awPass, project.awTenantCode))
    }

    project.task("configureArtifactDependency", dependsOn: "configureAirWatchEnvironment") << {
      project.dependencies.add("archives", "${project.group}:${project.target.toLowerCase()}:${project.artifactVersion}@ipa")
    }

    project.task("pushArtifactToAirWatch", dependsOn: "configureArtifactDependency") << {
      def ipaFile = project.configurations.archives.find { it.name.toLowerCase() =~ project.target.toLowerCase() }

      if (ipaFile == null) {
        throw new ResolveException("Could not find target specified. See available targets with `xcodebuild -list`.")
      }

      def transactionId = uploadFile(ipaFile)
      createApp(transactionId, project.target, project.target)
    }

    project.pushArtifactToAirWatch.group = "AirWatch"
    project.pushArtifactToAirWatch.description = "Distributes the app (.ipa) from Artifactory to AirWatch"
  }

  String uploadFile(File file) {
    def fileSize = file.size()
    def chunkSize = 5000
    def chunkSequenceNumber = 1
    def transactionId = "0"
    def totalChunks = "${new BigDecimal( Math.ceil(fileSize / chunkSize) )}"

    println "\nWill upload \"${file.name}\" to AirWatch..."

    file.eachByte(chunkSize) { buffer, sizeRead ->
      def bufferSlice = Arrays.copyOfRange(buffer, 0, sizeRead)
      def encodedChunk = bufferSlice.encodeBase64().toString()

      println "Uploading chunk ${chunkSequenceNumber} of ${totalChunks}..."

      transactionId = project.awClient.uploadChunk(transactionId, encodedChunk, chunkSequenceNumber, fileSize)

      chunkSequenceNumber++
    }

    transactionId
  }

  void createApp(transactionId, appName, appDescription) {
    println "\nWill create app in AirWatch using the uploaded chunks..."

    project.awClient.beginInstall(transactionId, appName, appDescription)
  }
}
