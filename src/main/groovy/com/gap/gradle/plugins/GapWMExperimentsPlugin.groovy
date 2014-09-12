package com.gap.gradle.plugins

import com.gap.gradle.ivy.IvyInfo
import org.gradle.api.Plugin
import org.gradle.api.Project

class GapWMExperimentsPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.repositories {
            ivy {
              name "wm_local_non_prod"
              layout "maven"
              url "http://artifactory.gapinc.dev/artifactory/local-non-prod"
              credentials {
                username "ec-build"
                password "Ec-art!"
              }
            }
            maven {
                name "wm_maven_remote_repos"
                url "http://artifactory.gapinc.dev/artifactory/remote-repos"
            }
            ivy {
                name "wm_ivy_remote_repos"
                layout "maven"
                url "http://artifactory.gapinc.dev/artifactory/remote-repos"
            }
        }
    }
}
