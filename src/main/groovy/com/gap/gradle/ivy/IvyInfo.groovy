package com.gap.gradle.ivy

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.internal.artifacts.result.DefaultUnresolvedDependencyResult

class IvyInfo {

    Project project
    Log logger = LogFactory.getLog(IvyInfo)

    IvyInfo(Project project) {
        this.project = project
    }

    def identifiers() {
        def identifiers = []
        project.allprojects.each { subProject ->
            identifiers.add(getIdentifier(subProject))
        }
        return identifiers as Set
    }

    def dependencies() {
        def dependencies = []
        project.allprojects.each { subProject ->
            subProject.configurations.each { config ->
                dependencies.addAll(getDependenciesFromConfiguration(config))
            }
        }

        return dependencies as Set
    }


    private Set getDependenciesFromConfiguration(Configuration config) {
        def dependencies = []
        config.dependencies.each { subProject ->
            dependencies.add(getIdentifier(subProject))
        }
        return dependencies as Set
    }

    def getAllResolvedDependencies() {
        def dependencies = []
        project.allprojects.each { subProject ->
            subProject.configurations.each { config ->
                config.getIncoming().getResolutionResult().allDependencies.each {
                    if(it instanceof DefaultUnresolvedDependencyResult) {
                        logger.info("Resolve failed - ${it.toString()}")
                    } else {
                        dependencies.add(it.selected.toString())
                    }
                }

            }
        }
        return dependencies.unique()
    }

    def Set getLeafDepsFromDepGraph() {
        def dependencies = []
        project.allprojects.each { subProject ->
            subProject.configurations.each { config ->
                config.getIncoming().getResolutionResult().getAllComponents().each {
                    //I thought it would be good to use getAllComponents rather than getAllDependencies because the former method
                    //will return nodes and the latter will return edges.
                    //All the leaf dependencies will have 0 dependencies (children) and have more than 0 dependents (parents).
                    if (!it.dependents.empty && it.dependencies.empty && it.selectionReason.description.equals("requested")) {
                        dependencies.add(it.id.toString())
                    }
                }
                /*config.getIncoming().getResolutionResult().getAllDependencies().each {
                    //We're selecting dependencies which are requested from ISO segment.
                    if (it.from.toString() =~ /\.iso/) {
                        dependencies.add(it.toString())
                    }
                }*/
            }
        }
        return dependencies as Set
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
