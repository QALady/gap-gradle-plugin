package com.gap.gradle.plugins

import com.gap.gradle.airwatch.*
import org.gradle.api.GradleException
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
    private Copy extractAirwatchConfigTask
    private AirWatchClientFactory airWatchClientFactory = new AirWatchClientFactory()
    private CredentialProvider credentialProvider = new CredentialProvider()
    private BeginInstallConfigValidator beginInstallConfigValidator = new BeginInstallConfigValidator()

    @Inject
    public AirWatchPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        this.project = project
        this.extractAirwatchConfigTask = createExtractAirwatchConfigTask()

        this.extension = project.extensions.create("airwatchUpload", AirwatchUploadExtension, project, instantiator, extractAirwatchConfigTask)
        this.extension.environments.add(preProductionEnv())
        this.extension.environments.add(productionEnv())

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
        def pushArtifactToAirWatchTask = project.task("pushArtifactToAirWatch") << {
            def artifactFinder = new ArtifactFinder(extension.artifact)
            def resolvedArtifact = project.configurations['archives'].resolvedConfiguration.resolvedArtifacts.find {
                def matchResult = artifactFinder.matches(it)
                println "Artifact ${it} from archives ${matchResult ? 'matches':'does not match'} artifact spec."
                matchResult
            }

            if (resolvedArtifact == null) {
                throw new GradleException("Could not find artifact that matches configured artifact in archives configuration.")
            }

            def targetEnvironment = extension.targetEnvironment
            if (targetEnvironment == null) {
                throw new GradleException("You need to specify to which environment the artifact will be uploaded using `targetEnvironment`.")
            }

            beginInstallConfigValidator.validate(extension)

            println "Pushing artifact to Airwatch ${targetEnvironment}..."

            def airwatchClient = airWatchClientFactory.create(targetEnvironment, credentialProvider)
            def createdApp = airwatchClient.uploadApp(resolvedArtifact.file, extension)

            ext.airwatchClient = airwatchClient
            ext.targetEnvironment = targetEnvironment
            ext.publishedAppId = createdApp["Id"]["Value"]
        }

        project.pushArtifactToAirWatch.group = "AirWatch"
        project.pushArtifactToAirWatch.description = "Distributes the app (.ipa) from Artifactory to AirWatch"

        project.task("autoAssignSmartGroups", dependsOn: "pushArtifactToAirWatch") {
            doFirst {
                String smartGroups = extension.smartGroups
                String locationGroupId = extension.targetEnvironment.locationGroupId
                String appId = pushArtifactToAirWatchTask.publishedAppId

                def airwatchClient = pushArtifactToAirWatchTask.airwatchClient

                airwatchClient.assignSmartGroupToApplication(smartGroups, appId, locationGroupId)
            }

            onlyIf { extension.smartGroups }
        }

        project.task("installAirwatchGem", type: Exec) {
            executable 'bundle'
            args = ['install', '--path', '/tmp/bundle']
            onlyIf { extension.configFile.exists() }
        }

        project.task("configureApp", type: Exec, dependsOn: ["installAirwatchGem", "extractAirwatchConfig", "pushArtifactToAirWatch"]) {
            executable 'bundle'

            doFirst {
                def credential = credentialProvider.get(extension.targetEnvironment.credentialName)
                args = ['exec', 'airwatch-app-config', extension.configFile, pushArtifactToAirWatchTask.publishedAppId, extension.appName]
                environment AW_URL: extension.targetEnvironment.consoleHost, AW_USER: credential.username, AW_PASS: credential.password
            }

            onlyIf { extension.configFile.exists() }
        }
    }

    def productionEnv() {
        new Environment("production").with {
            apiHost = "https://gapstoresds.awmdm.com/"
            consoleHost = "https://gapstoresds.awmdm.com/"
            tenantCode = "1VOJHIBAAAG6A46QCFAA"
            credentialName = "AirWatchProd"
            locationGroupId = "570"
            return it
        }
    }

    def preProductionEnv() {
        new Environment("preProduction").with {
            apiHost = "https://cn377.awmdm.com/"
            consoleHost = "https://cn377.awmdm.com/"
            tenantCode = "1AVBHIBAAAG6A4NQCFAA"
            credentialName = "AirWatchPreProd"
            locationGroupId = "575"
            return it
        }
    }

    void setAirWatchClientFactory(AirWatchClientFactory airWatchClientFactory) {
        this.airWatchClientFactory = airWatchClientFactory
    }

    void setCredentialProvider(CredentialProvider credentialProvider) {
        this.credentialProvider = credentialProvider
    }

    void setBeginInstallConfigValidator(BeginInstallConfigValidator beginInstallConfigValidator) {
        this.beginInstallConfigValidator = beginInstallConfigValidator
    }
}
