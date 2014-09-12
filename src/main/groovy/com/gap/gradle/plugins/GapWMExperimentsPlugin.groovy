package com.gap.gradle.plugins

import com.gap.gradle.ivy.IvyInfo
import org.gradle.api.Plugin
import org.gradle.api.Project

class GapWMExperimentsPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.repositories {
            ivy {
              name "wm-local-non-prod"
              layout "maven"
              url "http://artifactory.gapinc.dev/artifactory/local-non-prod"
              credentials {
                username "ec-build"
                password "Ec-art!"
              }
            }
        }
    }
}
