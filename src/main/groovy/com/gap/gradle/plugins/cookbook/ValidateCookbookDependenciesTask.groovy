package com.gap.gradle.plugins.cookbook
import static java.util.regex.Pattern.quote

import org.gradle.api.Project

class ValidateCookbookDependenciesTask {

    private static final def ranges = /[${[ "<", "<=", ">=", "~>", ">" ].each { t -> quote(t) }.join("|")}]/

    Project project

    ValidateCookbookDependenciesTask(Project project) {
        this.project = project
    }

    def execute() {
        if (project.chef.requirePinnedDependencies) {
            requireMetadata()
            validateDependencies()
        }
    }

    def requireMetadata() {
        if (project.chef.metadata == null) {
            throw new IllegalStateException("No chef metadata found on project!")
        }
    }

    def validateDependencies() {
        def dependencies = project.chef.metadata.dependencies;
        if (dependencies) {
            for (def entry : dependencies) {
                if (entry.value == "") {
                    throw new UnpinnedDependencyException(
                        "Cookbook dependency '" + entry.key + "' has no version pinned"
                    )
                } else if (entry.value =~ ranges) {
                    throw new UnpinnedDependencyException(
                        "Cookbook dependency '" + entry.key + "' version '" + entry.value + "' is not pinned"
                    )
                }
            }
        }
    }
}
