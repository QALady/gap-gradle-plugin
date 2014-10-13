package com.gap.gradle.plugins

import com.gap.gradle.airwatch.AirWatchClient
import com.gap.gradle.airwatch.AirwatchUploadExtension
import com.gap.gradle.airwatch.ArtifactFinder
import com.gap.pipeline.ec.CommanderClient
import org.gradle.api.GradleException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject

class AirWatchPlugin implements Plugin<Project> {

    private final Instantiator instantiator
    private Project project
    private AirwatchUploadExtension extension
    private CommanderClient commanderClient = new CommanderClient()
    private Copy extractAirwatchConfigTask

    public void setCommanderClient(CommanderClient commanderClient) {
        this.commanderClient = commanderClient
    }

    @Inject
    public AirWatchPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        this.project = project
        this.extractAirwatchConfigTask = createExtractAirwatchConfigTask()

        this.extension = project.extensions.create("airwatchUpload", AirwatchUploadExtension, instantiator, extractAirwatchConfigTask)

        createTasks()
    }

    Copy createExtractAirwatchConfigTask() {
        project.task("extractAirwatchConfig", type: Copy) {
            from {
                project.configurations.archives.resolvedConfiguration.resolvedArtifacts
                        .findAll { it.classifier == 'airwatchConfig' }
                        .collect { project.zipTree(it.file) }
            }
            into 'airwatchConfig'
        } as Copy
    }

    void createTasks() {
        project.task("validateProperties") << {
            if (!project.hasProperty("awEnv")) {
                throw new InvalidUserDataException("You need to define the AirWatch environment you want to push to, e.g. -PawEnv=CN11.")
            }

            if (!project.hasProperty("aw${project.awEnv}Host") ||
                !project.hasProperty("aw${project.awEnv}CredentialName") ||
                !project.hasProperty("aw${project.awEnv}TenantCode") ||
                !project.hasProperty("aw${project.awEnv}LocationGroupID")) {

                throw new InvalidUserDataException("Environment ${project.awEnv} is not defined. You should have the following entries in your gradle.properties file:\n" +
                        "- aw${project.awEnv}Host\n" +
                        "- aw${project.awEnv}CredentialName\n" +
                        "- aw${project.awEnv}TenantCode\n" +
                        "- aw${project.awEnv}LocationGroupID")
            }
        }

        project.task("getCredentials", dependsOn: "validateProperties") << {
            def credentialPath = "/projects/WM Credentials/credentials"
            def credentialName = project.get("aw${project.awEnv}CredentialName")

            ['userName', 'password'].each { valueName ->
                ext[valueName] = {
                    //commanderClient.getCredential("$credentialPath/$credentialName", valueName)
                    // TODO undo...
                    "gapcn11"
                }
            }
        }

        project.task("configureAirWatchEnvironment", dependsOn: "getCredentials") << {
            def client = new AirWatchClient(project.get("aw${project.awEnv}Host"),
                                            project.tasks.getCredentials.userName(),
                                            project.tasks.getCredentials.password(),
                                            project.get("aw${project.awEnv}TenantCode"))

            project.set("awClient", client)

            project.set("aw${project.awEnv}User", project.tasks.getCredentials.userName())
            project.set("aw${project.awEnv}Pass", project.tasks.getCredentials.password())
        }

        def pushArtifactToAirWatchTask = project.task("pushArtifactToAirWatch", dependsOn: "configureAirWatchEnvironment") << {
            def artifactFinder = new ArtifactFinder(extension.artifact)
            def resolvedArtifact = project.configurations['archives'].resolvedConfiguration.resolvedArtifacts.find {
                def matchResult = artifactFinder.matches(it)
                println "Artifact ${it} from archives ${matchResult ? 'matches':'does not match'} artifact spec."
                matchResult
            }

            if (resolvedArtifact == null) {
                throw new GradleException("Could not find artifact that matches configured artifact in archives configuration.")
            }

            def createdApp = project.awClient.uploadApp(resolvedArtifact.file, extension.appName, extension.appDescription, project.get("aw${project.awEnv}LocationGroupID"))

            ext.publishedAppId = createdApp["Id"]["Value"]
        }

        project.task("installAirwatchGem", type: Exec) {
            executable 'bundle'
            args = ['install', '--path', '/tmp/bundle']
        }

        project.task("configureApp", type: Exec, dependsOn: ["installAirwatchGem", "extractAirwatchConfig", "pushArtifactToAirWatch"]) {
            executable 'bundle'

            doFirst {
                args = ['exec', 'airwatch-app-config', extension.configFile(), pushArtifactToAirWatchTask.publishedAppId, extension.appName]
                environment AW_URL: project.get("aw${awEnv}WebHost"), AW_USER: project.get("aw${awEnv}User"), AW_PASS: project.get("aw${awEnv}Pass")
            }

            onlyIf { extension.configFile.exists() }
        }

        project.pushArtifactToAirWatch.group = "AirWatch"
        project.pushArtifactToAirWatch.description = "Distributes the app (.ipa) from Artifactory to AirWatch"
    }
}
