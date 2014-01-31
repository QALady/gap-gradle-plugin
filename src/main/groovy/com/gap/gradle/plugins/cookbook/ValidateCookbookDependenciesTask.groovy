package com.gap.gradle.plugins.cookbook
import static java.util.regex.Pattern.quote

import com.gap.gradle.chef.CookbookUtil
import org.gradle.api.Project

class ValidateCookbookDependenciesTask {

    private static final def ranges = [ "<", "<=", ">=", "~>", ">" ].each { t -> quote(t) }.join("|")

    Project project

    ValidateCookbookDependenciesTask(Project project) {
        this.project = project
    }

    def execute() {
        def dependencies = new CookbookUtil().metadataFrom().dependencies;
        if (dependencies) {
            for (def entry : dependencies) {
                if (entry.value == "") {
                    throw new UnpinnedDependencyException(
                        "Cookbook dependency '" + entry.key + "' has no version pinned"
                    )
                } else if (entry.value =~ /[$ranges]/) {
                    throw new UnpinnedDependencyException(
                        "Cookbook dependency '" + entry.key + "' version '" + entry.value + "' is not pinned"
                    )
                }
            }
        }
    }
}
