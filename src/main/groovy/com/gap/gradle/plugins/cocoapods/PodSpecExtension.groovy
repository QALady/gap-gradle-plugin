package com.gap.gradle.plugins.cocoapods

import com.gap.gradle.plugins.xcode.Property
import org.gradle.api.Project

class PodSpecExtension implements Podspec {

    private Project project
    private Property<String> podName
    private Property<String> podVersion

    PodSpecExtension(Project project) {
        this.project = project
    }

    void setPodName(Object podName) {
        this.podName = new Property(podName)
    }

    void setPodVersion(Object podVersion) {
        this.podVersion = new Property(podVersion)
    }

    @Override
    String getPodName() {
        return podName?.get()
    }

    @Override
    String getPodVersion() {
        return podVersion?.get() ?: getProjectVersion()
    }

    private String getProjectVersion() {
        return hasProjectVersion() ? project.version : null
    }

    private boolean hasProjectVersion() {
        project.hasProperty("version") && !project.version.equals("unspecified")
    }
}
