package com.gap.gradle.plugins.appium

import com.gap.gradle.plugins.mobile.CommandRunner
import com.gap.gradle.plugins.mobile.MobileDeviceUtils
import com.gap.gradle.tasks.SpawnBackgroundProcessTask
import com.gap.gradle.tasks.StopProcessByPortTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import com.gap.gradle.plugins.FileDownloader

class GapiOSTestAppiumPlugin implements Plugin<Project> {

    private static final int APPIUM_PORT = 4723
    private static final int IOS_DEBUG_PROXY_PORT = 27753

    private CommandRunner commandRunner
    private FileDownloader downloader

    void apply(Project project) {
        project.extensions.create('appiumConfig', AppiumPluginExtension,project)
        this.commandRunner = new CommandRunner(project)
        this.downloader = new FileDownloader(project)
        def templateLocation = project.hasProperty('defaultInstrumentsTemplatePath') ? project.getProperty('defaultInstrumentsTemplatePath') : ""

        project.task('startAppium', type: SpawnBackgroundProcessTask) {
            doFirst {
                project.appiumConfig.logFile.parentFile.mkdirs()
                command =  'appium' + project.appiumConfig.appiumServerArguments()
            }
        }

        project.task('startiOSWebkitDebugProxy', type: SpawnBackgroundProcessTask) {
            doFirst {
                command "node /usr/local/lib/node_modules/appium/bin/ios-webkit-debug-proxy-launcher.js -c ${connectedDeviceUdid}:27753 -d"
            }

            onlyIf { !project.appiumConfig.simulatorMode }
        }

        project.task('stopAppium', type: StopProcessByPortTask) {
            ports = [APPIUM_PORT, IOS_DEBUG_PROXY_PORT]
        }

        project.task('getPerfMetrics') {
            doFirst {
                def template = downloader.download(templateLocation, project.buildDir)
                project.appiumConfig.setExtendedServerFlags("--tracetemplate "+template)
                project.tasks.startAppium.execute()
            }
            onlyIf { !project.appiumConfig.simulatorMode }
        }

        project.tasks['startAppium'].dependsOn('startiOSWebkitDebugProxy')
    }

    private String getConnectedDeviceUdid() {
        def deviceUdid = new MobileDeviceUtils(commandRunner).listAttachedDevices()

        if (deviceUdid) {
            println "Connected device UDID: ${deviceUdid}"
        }

        deviceUdid
    }
}
