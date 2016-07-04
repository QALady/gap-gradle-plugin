package com.gap.gradle.plugins.iossigning

import org.gradle.api.Plugin
import org.gradle.api.Project

class GapiOSReSigningPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.task("ipaReSigning", type: IpaReSigningTask)
    }
}
