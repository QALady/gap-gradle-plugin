package com.gap.gradle.plugins.appium

import com.gap.gradle.plugins.mobile.CommandRunner
import com.gap.gradle.plugins.mobile.MobileDeviceUtils
import com.gap.gradle.tasks.SpawnBackgroundProcessTask
import com.gap.gradle.tasks.StopProcessByPortTask
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.gap.gradle.plugins.FileDownloader

class GapiOSTestAppiumPlugin implements Plugin<Project> {

    private static final int APPIUM_PORT = 4723
    private static final int IOS_DEBUG_PROXY_PORT = 27753
    private static final String INSTRUMENTS_TEMPLATE_URL = "http://github.gapinc.dev/mpl/instruments-standard-template/blob/master/snapserve-standard.tracetemplate?raw=true"

    private CommandRunner commandRunner
    private FileDownloader downloader
    private AppiumPluginExtension extension
    private Project project

    void apply(Project project) {
        this.project = project
        this.commandRunner = new CommandRunner(project)
        this.downloader = new FileDownloader(project)
        this.extension = project.extensions.create('appiumConfig', AppiumPluginExtension, project)

        project.task('startAppium', type: SpawnBackgroundProcessTask, dependsOn: 'startiOSWebkitDebugProxy') {
            doFirst {
                extension.logFile.parentFile.mkdirs()

                if (!extension.simulatorMode) {
                    extension.setExtendedServerFlags("--udid " + connectedDeviceUdid)
                }

                command =  'appium ' + extension.appiumServerArguments()
            }
        }

        project.task('startiOSWebkitDebugProxy', type: SpawnBackgroundProcessTask) {
            doFirst {
                command "node /usr/local/lib/node_modules/appium/bin/ios-webkit-debug-proxy-launcher.js -c ${connectedDeviceUdid}:27753 -d"
            }

            onlyIf { !extension.simulatorMode }
        }

        project.task('stopAppium', type: StopProcessByPortTask) {
            ports = [APPIUM_PORT, IOS_DEBUG_PROXY_PORT]
        }

        project.task('startAppiumForPerformanceTests', dependsOn: 'startiOSWebkitDebugProxy') {
            doFirst {
                extension.setExtendedServerFlags("--tracetemplate " + traceTemplate)
                project.tasks.startAppium.execute()
            }

            onlyIf { !extension.simulatorMode }
        }
    }

    private File getTraceTemplate() {
        def downloadDir = project.buildDir

        println "Downloading Instruments trace template from ${INSTRUMENTS_TEMPLATE_URL} into ${downloadDir}...\n"

        downloader.download(INSTRUMENTS_TEMPLATE_URL, downloadDir)
    }

    private String getConnectedDeviceUdid() {
        def deviceUdid = new MobileDeviceUtils(commandRunner).listAttachedDevices()

        if (deviceUdid.isEmpty()) {
            throw new GradleException("No iPod device detected. Please connect a device or use `Simulator Mode`")
        }

        println "Connected device UDID: ${deviceUdid}"

        deviceUdid
    }
}
