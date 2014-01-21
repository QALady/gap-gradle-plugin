package com.gap.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin

class GapGemJarPlugin implements Plugin<Project> {

  void apply(Project target) {
    target.configure(target) {
      repositories {
        maven {
          url 'http://repo1.phx.gapinc.dev/artifactory/gemjars'
        }
      }
    }
  }

}
