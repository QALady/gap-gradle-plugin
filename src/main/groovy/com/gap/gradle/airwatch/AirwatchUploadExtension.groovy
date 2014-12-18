package com.gap.gradle.airwatch
import org.gradle.api.Action
import org.gradle.api.NamedDomainObjectCollection
import org.gradle.api.NamedDomainObjectSet
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.internal.reflect.Instantiator

class AirwatchUploadExtension implements BeginInstallConfig {
    private Object appNameObj
    private Object appDescriptionObj
    private Object smartGroupsObj
    final ArtifactSpec artifact
    final NamedDomainObjectSet<Environment> environments
    String configFilename
    Environment targetEnvironment
    File configFile
    String pushMode
    Integer uploadChunks

    private final Copy extractAirwatchConfigTask
    private final Instantiator instantiator
    private final Project project

    AirwatchUploadExtension(Project project, Instantiator instantiator, Copy extractAirwatchConfigTask) {
        this.project = project
        this.instantiator = instantiator
        this.extractAirwatchConfigTask = extractAirwatchConfigTask
        this.artifact = instantiator.newInstance(ArtifactSpec)
        this.environments = project.container(Environment, { name -> instantiator.newInstance(Environment, name) })
        this.uploadChunks = 25
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

    void setSmartGroups(Object smartGroup) {
        this.smartGroupsObj = smartGroup
    }

    String getSmartGroups() {
        if (smartGroupsObj instanceof Closure) {
            return smartGroupsObj.call()
        }
        return smartGroupsObj
    }

    @Override
    String getLocationGroupId() {
        targetEnvironment.locationGroupId
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

    void environments(Action<? super NamedDomainObjectCollection<Environment>> action) {
        action.execute(environments)
    }
}
