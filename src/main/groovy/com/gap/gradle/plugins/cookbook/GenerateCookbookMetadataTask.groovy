package com.gap.gradle.plugins.cookbook

import com.gap.gradle.chef.CookbookUtil
import org.gradle.api.Project

class GenerateCookbookMetadataTask {

    Project project

    GenerateCookbookMetadataTask(Project project) {
        this.project = project
    }

    def execute() {
        project.chef.metadata = new CookbookUtil().metadataFrom(project.chef.cookbookDir)
    }
}
