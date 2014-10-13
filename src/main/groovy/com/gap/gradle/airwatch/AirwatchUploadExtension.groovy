package com.gap.gradle.airwatch

import org.gradle.api.Action
import org.gradle.api.tasks.Copy
import org.gradle.internal.reflect.Instantiator

class AirwatchUploadExtension {
    private Object appNameObj
    private Object appDescriptionObj
    String configFilename
    final ArtifactSpec artifact
    File configFile

    private final Copy extractAirwatchConfigTask
    private final Instantiator instantiator

    AirwatchUploadExtension(Instantiator instantiator, Copy extractAirwatchConfigTask) {
        this.instantiator = instantiator
        this.extractAirwatchConfigTask = extractAirwatchConfigTask
        this.artifact = instantiator.newInstance(ArtifactSpec)
    }

    void setAppName(Object appName) {
        this.appNameObj = appName
    }

    String getAppName() {
        if (appNameObj instanceof Closure) {
            return appNameObj.call()
        }
        appNameObj
    }

    void setAppDescription(Object appDescription) {
        this.appDescriptionObj = appDescription
    }

    String getAppDescription() {
        if (appDescriptionObj instanceof Closure) {
            return appDescriptionObj.call()
        }
        appDescriptionObj
    }

    File getConfigFile() {
        if (configFile) {
            return configFile
        }
        new File(extractAirwatchConfigTask.destinationDir, configFilename)
    }

    void artifact(Action<ArtifactSpec> action) {
        action.execute(artifact)
    }
}
