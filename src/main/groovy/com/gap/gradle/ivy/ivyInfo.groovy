package com.gap.gradle.ivy

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration




class IvyInfo {

    Project project

    IvyInfo(Project project){
        this.project = project
    }

    def identifiers() {
        def identifiers = []
        project.allprojects.each {subProject ->
            identifiers.add(getIdentifier(subProject))
        }
        return identifiers as Set
    }

    def dependencies() {
        def dependencies = []
        project.allprojects.each {subProject ->
            subProject.configurations.each { config ->
               dependencies.addAll(getDependenciesFromConfiguration(config))
            }
        }

        return dependencies as Set
    }

    private List getDependenciesFromConfiguration(Configuration config) {
        def dependencies = []
        config.dependencies.each {subProject ->
            dependencies.add(getIdentifier(subProject))
        }
        return dependencies
    }

    private GString getIdentifier(def module) {
        "${module.group}:${module.name}"
    }


    def version() {
        return project.version
    }

    boolean equals(o) {
        if (this.is(o)) return true
        if (getClass() != o.class) return false

        IvyInfo ivyInfo = (IvyInfo) o

        if (project != ivyInfo.project) return false

        return true
    }

    int hashCode() {
        return (project != null ? project.hashCode() : 0)
    }
}
