package com.gap.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin

class GapBuildPlugin implements Plugin<Project>{
  void apply(Project target) {
    target.configure(target) {

      evaluationDependsOnChildren()

      repositories {
        maven {
          url "${gradle.gradleUserHomeDir}/local-repo"
        }

        maven {
          url 'http://artifactory.gapinc.dev/artifactory/local-non-prod'
        }
      }

      if (target.plugins.hasPlugin('java')) {
        apply plugin: com.gap.gradle.plugins.resources.GapResourcesPlugin

        dependencies {
          testCompile 'junit:junit:4.11'
        }

        target.tasks.test.enableAssertions false
        sourceSets.test.java.srcDirs 'src/test/unit/java'
        sourceSets.test.resources.srcDirs 'src/test/unit/resources'
      }

    }
  }
}
