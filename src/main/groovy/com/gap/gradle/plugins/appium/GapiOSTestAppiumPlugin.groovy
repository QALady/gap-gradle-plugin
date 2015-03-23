package com.gap.gradle.plugins.appium

import com.gap.gradle.plugins.mobile.CommandRunner
import com.gap.gradle.plugins.mobile.MobileDeviceUtils
import com.gap.gradle.tasks.StartBackgroundProcessTask
import com.gap.gradle.tasks.StopProcessByPidTask
import com.gap.gradle.utils.FileDownloader
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

import static java.lang.System.getProperty

class GapiOSTestAppiumPlugin implements Plugin<Project> {

    private static final String INSTRUMENTS_TEMPLATE_URL = 'http://github.gapinc.dev/mpl/instruments-standard-template/raw/master/snapserve-standard.tracetemplate'

    private AppiumPluginExtension extension
    private Project project
    private File tempPidFile
    private File instrumentsTraceOutputDir

    void apply(Project project) {
        this.project = project
        this.extension = project.extensions.create('appiumConfig', AppiumPluginExtension, project)
        this.tempPidFile = createTempFile(project)
        this.instrumentsTraceOutputDir = project.file("${project.buildDir}/test/instruments-trace-results")

        createTasks(project)
    }

    private void createTasks(Project project) {
        project.task('configureAppiumForRealDevices') {
            doFirst {
                instrumentsTraceOutputDir.mkdirs()

                extension.setExtendedServerFlags("--udid " + connectedDeviceUdid)
                extension.setExtendedServerFlags("--tracetemplate " + traceTemplate)
                extension.setExtendedServerFlags("--trace-dir " + instrumentsTraceOutputDir.absolutePath)
            }

            onlyIf { !extension.simulatorMode }
        }

        project.task('startiOSWebkitDebugProxy', type: StartBackgroundProcessTask) {
            doFirst {
                command "node /usr/local/lib/node_modules/appium/bin/ios-webkit-debug-proxy-launcher.js -c ${connectedDeviceUdid}:27753 -d"
                pidFile tempPidFile
            }

            onlyIf { !extension.simulatorMode }
        }

        project.task('startAppium', type: StartBackgroundProcessTask, dependsOn: ['configureAppiumForRealDevices', 'startiOSWebkitDebugProxy']) {
            doFirst {
                extension.logFile.parentFile.mkdirs()

                command 'appium ' + extension.appiumServerArguments()
                pidFile tempPidFile
            }
        }

        project.task('stopAppium', type: StopProcessByPidTask) {
            pidFile tempPidFile
        }

        project.task('zipInstrumentsTraceResults', type: Zip) {
            from instrumentsTraceOutputDir

            doLast {
                project.artifacts {
                    archives(project.zipInstrumentsTraceResults) {
                        name instrumentsTraceOutputDir.name
                    }
                }
            }
        }

        project.getTasksByName('uploadArchives', false).each {
            it.dependsOn 'zipInstrumentsTraceResults'
        }
    }

    private File getTraceTemplate() {
        def downloadDir = project.buildDir

        println "Downloading Instruments trace template from ${INSTRUMENTS_TEMPLATE_URL} into ${downloadDir}...\n"

        new FileDownloader(project).download(INSTRUMENTS_TEMPLATE_URL, downloadDir)
    }

    private String getConnectedDeviceUdid() {
        def commandRunner = new CommandRunner(project)
        def deviceUdid = new MobileDeviceUtils(commandRunner).listAttachedDevices()

        if (deviceUdid.isEmpty()) {
            throw new GradleException("No iPod device detected. Please connect a device or use `Simulator Mode`")
        }

        println "Connected device UDID: ${deviceUdid}"

        deviceUdid
    }

    private static File createTempFile(Project project) {
        def file = new File(getProperty('java.io.tmpdir'), "${project.name}.pids")

        if (!file.exists()) file.createNewFile()

        file
    }
}
