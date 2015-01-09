package com.gap.gradle.airwatch

import com.gap.gradle.airwatch.util.Barrier
import com.gap.gradle.airwatch.util.CaptureExecOutput
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.concurrent.TimeUnit

class WaitDeviceToGetAppTask extends DefaultTask {

    private static final String INSTALLED_STATUS = "2"

    CaptureExecOutput capture = new CaptureExecOutput(project)

    AirWatchClient airwatchClient
    int numberOfTries
    long sleepTimeout
    TimeUnit sleepTimeUnit
    File publishedArtifactFile

    @TaskAction
    def waitDeviceToGetApp() {
        String connectedDeviceUdid = getConnectedDeviceUdid()

        File infopList = project.zipTree(publishedArtifactFile).matching {
            include 'Payload/*.app/Info.plist'
        }.getSingleFile()

        String appBundleIdentifier = readPlistKeyValue(infopList, "CFBundleIdentifier")
        String appBundleVersion = readPlistKeyValue(infopList, "CFBundleVersion")

        queryDeviceForRequiredAppUntilTimeout(connectedDeviceUdid, appBundleIdentifier, appBundleVersion)
    }

    private void queryDeviceForRequiredAppUntilTimeout(String connectedDeviceUdid, String appBundleIdentifier, String appBundleVersion) {
        try {
            new Barrier(numberOfTries, sleepTimeout, sleepTimeUnit).executeUntil {
                println "\nQuerying the device apps..."

                airwatchClient.queryDevice(connectedDeviceUdid)

                def mapAppNameToInfo = airwatchClient.getDeviceApps(connectedDeviceUdid)
                def hasAppInResults = mapAppNameToInfo.containsKey(appBundleIdentifier)
                def hasRequiredVersion = hasAppInResults && mapAppNameToInfo[appBundleIdentifier]["BuildVersion"] == appBundleVersion
                def isAppInstalled = hasAppInResults && mapAppNameToInfo[appBundleIdentifier]["Status"].toString() == INSTALLED_STATUS

                println "App listed in device apps? ${hasAppInResults}; Is app installed? ${isAppInstalled}; Is required version? ${hasRequiredVersion}"

                hasRequiredVersion && isAppInstalled
            }
        } catch (Barrier.MaxNumberOfTriesReached e) {
            throw new RuntimeException("Device did not receive the required application version number " +
                    "(bundle identifier = $appBundleIdentifier, build version = $appBundleVersion).", e)
        }
    }

    private String readPlistKeyValue(File plist, String key) {
        def result = capture.outputOf("/usr/libexec/PlistBuddy", plist, "-c", "Print :${key}").trim()

        println "\n\n Value from plist for key $key = $result \n\n"

        result
    }

    private String getConnectedDeviceUdid() {
        def deviceUdid = capture.outputOf("bash", "-c", "system_profiler SPUSBDataType | sed -n -e '/iPod/,/Serial/p' | grep 'Serial Number:' | cut -d ':' -f 2").trim()

        if (deviceUdid.isEmpty()) {
            throw new RuntimeException("No iPod device detected.")
        }

        println "\n\nConnected device UDID = $deviceUdid \n\n"

        deviceUdid
    }
}
