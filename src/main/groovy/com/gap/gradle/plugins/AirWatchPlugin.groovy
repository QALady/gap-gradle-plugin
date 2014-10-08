package com.gap.gradle.plugins

import com.gap.gradle.airwatch.AirWatchClient
import com.gap.pipeline.ec.CommanderClient
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolveException

class AirWatchPlugin implements Plugin<Project> {

    private Project project
    private CommanderClient commanderClient = new CommanderClient()

    public void setCommanderClient(CommanderClient commanderClient) {
        this.commanderClient = commanderClient
    }

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
                !project.hasProperty("aw${project.awEnv}CredentialName") ||
                !project.hasProperty("aw${project.awEnv}TenantCode") ||
                !project.hasProperty("aw${project.awEnv}LocationGroupID"))

                throw new InvalidUserDataException("Environment ${project.awEnv} is not defined. You should have the following entries in your gradle.properties file:\n" +
                    "- aw${project.awEnv}Host\n" +
                    "- aw${project.awEnv}CredentialName\n" +
                    "- aw${project.awEnv}TenantCode\n" +
                    "- aw${project.awEnv}LocationGroupID")
        }

        project.task("getCredentials", dependsOn: "validateProperties") << {
            def credentialName = project.get("aw${project.awEnv}CredentialName")

            ['userName', 'password'].each { valueName ->
                ext[valueName] = {
                    commanderClient.getCredential(credentialName, valueName)
                }
            }
        }

        project.task("configureAirWatchEnvironment", dependsOn: "getCredentials") << {
            def client = new AirWatchClient(
                project.get("aw${project.awEnv}Host"),
                project.tasks.getCredentials.userName(),
                project.tasks.getCredentials.password(),
                project.get("aw${project.awEnv}TenantCode"),
                project.get("aw${project.awEnv}LocationGroupID"))

            project.set("awClient", client)

            project.set("aw${project.awEnv}User", project.tasks.getCredentials.userName())
            project.set("aw${project.awEnv}Pass", project.tasks.getCredentials.password())
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

    Map createApp(transactionId, appName, appDescription) {
        println "\nWill create app in AirWatch using the uploaded chunks..."

        project.awClient.beginInstall(transactionId, appName, appDescription)
    }
}
