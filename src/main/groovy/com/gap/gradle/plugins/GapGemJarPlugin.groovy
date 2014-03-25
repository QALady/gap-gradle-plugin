package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class GapGemJarPlugin implements Plugin<Project> {

  void apply(Project target) {
    target.configure(target) {
      repositories {
        maven {
          url 'http://artifactory.gapinc.dev/artifactory/gemjars'
        }
      }
    }
  }

}
