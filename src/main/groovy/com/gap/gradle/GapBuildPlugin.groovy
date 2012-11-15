package com.gap.gradle

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
          url 'http://ci.gap.dev/artifactory/generic-repo'
        }
      }

      if (target.plugins.hasPlugin('java')) {
        apply plugin: com.gap.gradle.resources.GapResourcesPlugin

        dependencies {
          testCompile 'junit:junit:4.10'
        }

        target.tasks.test.enableAssertions false
        sourceSets.test.java.srcDirs 'src/test/unit/java'
        sourceSets.test.resources.srcDirs 'src/test/unit/resources'
      }

      //print Ivy identifiers task
      target.task('ivyIdentifiers') << {
        println project.group + ":" + project.name
      }

      //print Ivy dependencies task
      target.task('ivyDependencies') << {
        configurations.each { 
          config -> config.dependencies.each { 
            dep -> println dep.group + ":" + dep.name 
          } 
        }
      }
    
      //unzip integration test dependencies task
      target.task('unzipIntegrationTests') << {
        configurations.integrationTest.files.each {
          file -> copy {
            from zipTree(file.path)
            into '.'
          }
        }
      }

    }
  }
}
