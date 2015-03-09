package com.gap.gradle.plugins.appium

import com.gap.gradle.tasks.SpawnBackgroundProcessTask
import com.gap.gradle.tasks.StopProcessByPortTask
import org.gradle.api.Plugin
import org.gradle.api.Project


class GapiOSTestAppiumPlugin implements Plugin<Project> {

    private static final int APPIUM_PORT = 4723
    private static final int IOS_DEBUG_PROXY_PORT = 27753

    void apply(Project project) {

        project.extensions.create('appiumConfig', AppiumPluginExtension,project)

        project.task('startAppium', type: SpawnBackgroundProcessTask) {

            project.afterEvaluate {
                command =  'appium' + project.appiumConfig.appiumServerArguments()
                println "starting background process for Appium " + command
            }

        }

        project.task('startiOSWebkitDebugProxy', type: SpawnBackgroundProcessTask) {

            def wdir = new File('.')
            def cmd = "system_profiler SPUSBDataType | sed -n -e '/iPod/,/Serial/p' | grep 'Serial Number:' | cut -d ':' -f 2 | tr -d ' '"
            def UDID = ["bash", "-c", cmd].execute(null, wdir).text.trim()
            println "UDID of the device associated to " + UDID
            command "node /usr/local/lib/node_modules/appium/bin/ios-webkit-debug-proxy-launcher.js -c ${UDID}:27753 -d"

            onlyIf { !project.appiumConfig.simulatorMode }
        }

        project.task('stopAppium', type: StopProcessByPortTask) {

            ports = [APPIUM_PORT, IOS_DEBUG_PROXY_PORT]

        }

        project.tasks['startAppium'].dependsOn('startiOSWebkitDebugProxy')

    }
}


