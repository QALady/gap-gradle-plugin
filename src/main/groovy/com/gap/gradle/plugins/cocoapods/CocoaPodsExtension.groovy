package com.gap.gradle.plugins.cocoapods

import com.gap.gradle.plugins.xcode.Property
import org.gradle.api.GradleException
import org.gradle.api.Project

class CocoaPodsExtension {

    private Project project
    private Property<String> podName
    private Property<String> podVersion

    CocoaPodsExtension(Project project) {
        this.project = project
    }

    String getPodName() {
        def podName = podName?.get()

        if (!podName) {
            throw new GradleException("Please define a `podName`.")
        }

        return podName
    }

    void setPodName(Object podName) {
        this.podName = new Property(podName)
    }

    String getPodVersion() {
        return podVersion?.get() ?: getProjectVersion()
    }

    void setPodVersion(Object podVersion) {
        this.podVersion = new Property(podVersion)
    }

    private String getProjectVersion() {
        if (projectVersionIsDefined()) {
            return project.version
        }

        throw new GradleException("Unable to find pod version. No `podVersion` or `project.version` defined.")
    }

    private boolean projectVersionIsDefined() {
        project.hasProperty("version") && !project.version.equals("unspecified")
    }
}
