package com.gap.gradle.plugins.airwatch

import com.gap.gradle.plugins.mobile.ArchivesArtifactFinder
import com.gap.gradle.plugins.mobile.CommandRunner
import com.gap.gradle.plugins.mobile.credentials.CredentialProvider
import com.gap.gradle.plugins.mobile.credentials.GitCryptCredentialProvider
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Exec
import org.gradle.internal.reflect.Instantiator

import javax.inject.Inject
import java.util.concurrent.TimeUnit

import static com.gap.gradle.plugins.airwatch.Environment.DEFAULT_PREPRODUCTION
import static com.gap.gradle.plugins.airwatch.Environment.DEFAULT_PRODUCTION

class AirWatchPlugin implements Plugin<Project> {

    private static final int NUMBER_OF_TRIES = 10

    private final Instantiator instantiator
    private Project project
    private AirwatchUploadExtension extension
    private Copy extractAirwatchConfigTask
    private CredentialProvider credentialProvider
    private AirWatchClientFactory airWatchClientFactory = new AirWatchClientFactory()
    private BeginInstallConfigValidator beginInstallConfigValidator = new BeginInstallConfigValidator()
    private File ipaToUpload

    @Inject
    public AirWatchPlugin(Instantiator instantiator) {
        this.instantiator = instantiator
    }

    @Override
    void apply(Project project) {
        this.project = project
        this.extractAirwatchConfigTask = createExtractAirwatchConfigTask()
        this.credentialProvider = new GitCryptCredentialProvider(new CommandRunner(project))

        this.extension = project.extensions.create("airwatchUpload", AirwatchUploadExtension, project, instantiator, extractAirwatchConfigTask)
        this.extension.environments.add(DEFAULT_PREPRODUCTION)
        this.extension.environments.add(DEFAULT_PRODUCTION)

        createTasks()

        project.gradle.taskGraph.whenReady { taskGraph ->
            if (taskGraph.hasTask(':checkIfVersionAlreadyExists') || taskGraph.hasTask(':pushArtifactToAirWatch')) {
                this.ipaToUpload = getIpaToBeUploaded()
            }
        }
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
        def searchAppToRetire = project.task("searchAppToRetire") {
            doFirst {
                def appList = airWatchClient.searchApplication(extension.searchParamsToRetireApp)

                if (!appList.isEmpty()) {
                    def app = appList["Application"][0]

                    println "The following ipa will be retired after the new version is deployed: ${app}\n"

                    ext.retireVersion = app["Id"]["Value"].toString()
                }
            }

            onlyIf { extension.searchParamsToRetireApp.hasSearchParams() }
        }

        def versionCheckTask = project.task("checkIfVersionAlreadyExists") << {
            def ipaDetails = new IpaDetails(project, ipaToUpload)

            def desiredBundleId = ipaDetails.bundleIdentifier
            def desiredVersion = ipaDetails.bundleVersion

            def searchParams = new SearchApplicationConfig().with {
                bundleId = desiredBundleId
                status = "Active"
                return it
            }

            def searchResults = airWatchClient.searchApplication(searchParams)
            def existingVersions = searchResults["Application"].collect {
                ["id": it["Id"]["Value"], "version": it["AppVersion"]]
            }

            def versionFound = existingVersions.find { it.version == desiredVersion }

            if (versionFound) {
                project.logger.warn("Version \"${desiredVersion}\" of \"${desiredBundleId}\" is already uploaded. Will not try to upload again.")
                ext.publishedAppId = versionFound.id
            }
        }

        def pushArtifactToAirWatchTask = project.task("pushArtifactToAirWatch", dependsOn: ["searchAppToRetire", "checkIfVersionAlreadyExists"]) {
            group = "AirWatch"
            description = "Distributes the app (.ipa) from Artifactory to AirWatch"

            doFirst {
                beginInstallConfigValidator.validate(extension)

                println "Pushing artifact \"${ipaToUpload.name}\" to Airwatch ${targetEnvironment}...\n"

                def createdApp = airWatchClient.uploadApp(ipaToUpload, extension)
                ext.publishedAppId = createdApp["Id"]["Value"]
            }

            onlyIf { !versionCheckTask.hasProperty("publishedAppId") }
        }

        project.task("autoAssignSmartGroups", dependsOn: "pushArtifactToAirWatch") {
            doFirst {
                String smartGroups = extension.smartGroups
                String locationGroupId = targetEnvironment.locationGroupId
                String appId = getPublishedAppId(versionCheckTask, pushArtifactToAirWatchTask)

                airWatchClient.assignSmartGroupToApplication(smartGroups, appId, locationGroupId)
            }

            onlyIf { extension.smartGroups }
        }

        project.task("autoRetireAppPreviousVersion", dependsOn: ["searchAppToRetire", "pushArtifactToAirWatch"]) {
            doFirst {
                def retireVersion = searchAppToRetire.retireVersion

                airWatchClient.retireApplication(retireVersion)
            }

            onlyIf { searchAppToRetire.hasProperty('retireVersion') }
        }

        project.task("installAirwatchGem", type: Exec) {
            executable 'bundle'
            args = ['install', '--path', '/tmp/bundle']
            onlyIf { extension.configFile.isFile() }
        }

        project.task("configureApp", type: Exec, dependsOn: ["installAirwatchGem", "extractAirwatchConfig", "pushArtifactToAirWatch"]) {
            executable 'bundle'

            doFirst {
                def publishedAppId = getPublishedAppId(versionCheckTask, pushArtifactToAirWatchTask)
                def credential = credentialProvider.get(extension.targetEnvironment.credentialName)
                args = ['exec', 'airwatch-app-config', extension.configFile, publishedAppId, extension.appName]
                environment AW_URL: extension.targetEnvironment.consoleHost, AW_USER: credential.username, AW_PASS: credential.password
            }

            onlyIf { extension.configFile.isFile() }
        }

        project.task("waitDeviceToGetApp", type: WaitDeviceToGetAppTask, dependsOn: "pushArtifactToAirWatch") {
            doFirst {
                airwatchClient = airWatchClient
                publishedArtifactFile = ipaToUpload
            }

            numberOfTries = NUMBER_OF_TRIES
            sleepTimeout = 1
            sleepTimeUnit = TimeUnit.MINUTES
        }
    }

    String getPublishedAppId(Task... tasks) {
        def task = tasks.find { it.hasProperty("publishedAppId") }

        if (task) {
            println "Using publishedAppId \"${task.publishedAppId}\" from task \"${task.name}\""
            return task.publishedAppId
        }

        throw new GradleException("App version not yet published to AirWatch")
    }

    File getIpaToBeUploaded() {
        if (extension.ipaFile) {
            return extension.ipaFile
        }

        ResolvedArtifact resolvedArtifact = new ArchivesArtifactFinder(project).find(extension.artifact)

        if (resolvedArtifact == null) {
            throw new GradleException("Could not find artifact that matches configured artifact in archives configuration.")
        }

        return resolvedArtifact.file
    }

    private AirWatchClient getAirWatchClient() {
        return airWatchClientFactory.create(targetEnvironment, credentialProvider)
    }

    private Environment getTargetEnvironment() {
        def targetEnvironment = extension.targetEnvironment

        if (!targetEnvironment) {
            throw new GradleException("You need to specify to which environment the artifact will be uploaded using `targetEnvironment`.")
        }

        return targetEnvironment
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
