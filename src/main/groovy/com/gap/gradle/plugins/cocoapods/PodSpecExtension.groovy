package com.gap.gradle.plugins.cocoapods

import com.gap.gradle.plugins.xcode.Property
import org.gradle.api.Project

class PodSpecExtension implements Podspec {

    private Project project
    private Property<String> name
    private Property<String> version
    private Property<String> sourceLocation

    PodSpecExtension(Project project) {
        this.project = project
    }

    void setName(Object name) {
        this.name = new Property(name)
    }

    void setVersion(Object version) {
        this.version = new Property(version)
    }

    void setSourceLocation(Object sourceLocation) {
        this.sourceLocation = new Property(sourceLocation)
    }

    @Override
    String getName() {
        return name?.get()
    }

    @Override
    String getVersion() {
        return version?.get() ?: getProjectVersion()
    }

    @Override
    String getSourceLocation() {
        return sourceLocation?.get()
    }

    private String getProjectVersion() {
        return hasProjectVersion() ? project.version : null
    }

    private boolean hasProjectVersion() {
        project.hasProperty("version") && !project.version.equals("unspecified")
    }
}
