package com.gap.gradle.plugins.iossigning

import org.gradle.api.Plugin
import org.gradle.api.Project

class GapiOSSigningPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.task("ipaSigning", type: IpaSigningTask)
    }
}
