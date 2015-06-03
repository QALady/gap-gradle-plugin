package com.gap.gradle.plugins.airwatch

import com.gap.gradle.utils.Barrier
import com.gap.gradle.plugins.mobile.CommandRunner
import com.gap.gradle.plugins.mobile.MobileDeviceUtils
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import org.apache.commons.logging.LogFactory

import java.util.concurrent.TimeUnit

class WaitDeviceToGetAppTask extends DefaultTask {

    private static final logger = LogFactory.getLog(WaitDeviceToGetAppTask)

    private static final String INSTALLED_STATUS = "2"

    CommandRunner commandRunner = new CommandRunner(project)

    AirWatchClient airwatchClient
    int numberOfTries
    long sleepTimeout
    TimeUnit sleepTimeUnit
    File publishedArtifactFile

    @TaskAction
    def waitDeviceToGetApp() {
        String connectedDeviceUdid = getConnectedDeviceUdid()

        def ipaDetails = new IpaDetails(project, publishedArtifactFile)

        queryDeviceForRequiredAppUntilTimeout(connectedDeviceUdid, ipaDetails.bundleIdentifier, ipaDetails.bundleVersion)
    }

    private void queryDeviceForRequiredAppUntilTimeout(String connectedDeviceUdid, String appBundleIdentifier, String appBundleVersion) {
        try {
            new Barrier(numberOfTries, sleepTimeout, sleepTimeUnit).executeUntil {
                logger.info( "\nQuerying the device apps...")

                airwatchClient.queryDevice(connectedDeviceUdid)

                def mapAppNameToInfo = airwatchClient.getDeviceApps(connectedDeviceUdid)
                def hasAppInResults = mapAppNameToInfo.containsKey(appBundleIdentifier)
                def hasRequiredVersion = hasAppInResults && mapAppNameToInfo[appBundleIdentifier]["BuildVersion"] == appBundleVersion
                def isAppInstalled = hasAppInResults && mapAppNameToInfo[appBundleIdentifier]["Status"].toString() == INSTALLED_STATUS

                logger.info( "App listed in device apps? ${hasAppInResults}; Is app installed? ${isAppInstalled}; Is required version? ${hasRequiredVersion}")

                hasRequiredVersion && isAppInstalled
            }
        } catch (Barrier.MaxNumberOfTriesReached e) {
            throw new GradleException("Device did not receive the required application version number " +
                    "(bundle identifier = $appBundleIdentifier, build version = $appBundleVersion).", e)
        }
    }

    private String getConnectedDeviceUdid() {
        def deviceUdid = new MobileDeviceUtils(commandRunner).listAttachedDevices()

        if (deviceUdid.isEmpty()) {
            throw new GradleException("No iPod device detected.")
        }

        logger.info( "Connected device UDID = $deviceUdid")

        deviceUdid
    }
}
