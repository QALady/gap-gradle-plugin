package com.gap.gradle.plugins.appium

import com.gap.gradle.plugins.airwatch.util.CommandRunner
import com.gap.gradle.plugins.mobile.MobileDeviceUtils
import com.gap.gradle.tasks.SpawnBackgroundProcessTask
import com.gap.gradle.tasks.StopProcessByPortTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GapiOSTestAppiumPlugin implements Plugin<Project> {

    private static final int APPIUM_PORT = 4723
    private static final int IOS_DEBUG_PROXY_PORT = 27753

    private CommandRunner commandRunner

    void apply(Project project) {
        project.extensions.create('appiumConfig', AppiumPluginExtension,project)
        this.commandRunner = new CommandRunner(project)

        project.task('startAppium', type: SpawnBackgroundProcessTask) {
            doFirst {
                project.appiumConfig.logFile.parentFile.mkdirs()
            }

            project.afterEvaluate {
                command =  'appium' + project.appiumConfig.appiumServerArguments()
                println "starting background process for Appium " + command
            }
        }

        project.task('startiOSWebkitDebugProxy', type: SpawnBackgroundProcessTask) {
            command "node /usr/local/lib/node_modules/appium/bin/ios-webkit-debug-proxy-launcher.js -c ${connectedDeviceUdid}:27753 -d"

            onlyIf { !project.appiumConfig.simulatorMode }
        }

        project.task('stopAppium', type: StopProcessByPortTask) {
            ports = [APPIUM_PORT, IOS_DEBUG_PROXY_PORT]
        }

        project.tasks['startAppium'].dependsOn('startiOSWebkitDebugProxy')
    }

    private String getConnectedDeviceUdid() {
        def deviceUdid = new MobileDeviceUtils(commandRunner).listAttachedDevices()

        if (deviceUdid) {
            println "\n\nConnected device UDID = $deviceUdid\n\n"
        }

        deviceUdid
    }
}
