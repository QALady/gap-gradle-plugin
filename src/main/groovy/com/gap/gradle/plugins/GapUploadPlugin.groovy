package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar

class GapUploadPlugin implements Plugin<Project>{

 
  void apply(Project project) {
  
    def ivyUrl      = project.hasProperty('ivyPublishUrl')      ? project.getProperty('ivyPublishUrl')      : "${project.gradle.gradleUserHomeDir}/local-repo";
    def ivyUsername = project.hasProperty('ivyPublishUsername') ? project.getProperty('ivyPublishUsername') : ""
    def ivyPassword = project.hasProperty('ivyPublishPassword') ? project.getProperty('ivyPublishPassword') : ""

    if (project.plugins.hasPlugin('java')) {

      project.tasks.add(name:'packageConfigs', type: Jar)
      {
        classifier = 'config'
        from project.sourceSets.main.resources 
        include "**/*.erb"
        include "${project.sourceSets.main.name}.rb"
      };

      project.tasks.add(name:'packageTests', type: Jar)
      {
        classifier = 'tests'
        from project.sourceSets.test.output
      }

      project.tasks.add(name:'packageIntegrationTests', type: Jar)
      {
        classifier = 'integrationTests'
        from "src/test/functional"
      }

      project.tasks.add(name:'packageSmokeTests', type: Jar)
      {
        classifier = 'smokeTests'
        from "src/test/smoke"
      }

      project.configure(project){
        uploadArchives {

          dependsOn('uploadTestRuntime', 'uploadConfig', 'uploadIntegrationTest', 'uploadSmokeTest')

          //Upload Project dependencies as well
          project.configurations.compile.dependencies.each{
            if (it instanceof org.gradle.api.artifacts.ProjectDependency)
              uploadArchives.dependsOn(it.dependencyProject.path + ':uploadArchives')}

          repositories {
            ivy {
              layout 'maven'
              url "${ivyUrl}"
              credentials {
                username "${ivyUsername}"
                password "${ivyPassword}"
              }
            }
          }
        }

        configurations {
          config
          testRuntime{visible true}
          integrationTest
          smokeTest
        }

        artifacts {
          config packageConfigs
          testRuntime packageTests
          integrationTest packageIntegrationTests
          smokeTest packageSmokeTests
        }

        uploadTestRuntime {
          repositories { uploadArchives.repositories.each{add it}} //Use same repos as uploadArchives
        }

        uploadConfig {
          repositories { uploadArchives.repositories.each{add it}} //Use same repos as uploadArchives
        }

        uploadIntegrationTest {
          repositories { uploadArchives.repositories.each{add it}} //Use same repos as uploadArchives
        }

        uploadSmokeTest {
          repositories { uploadArchives.repositories.each{add it}} //Use same repos as uploadArchives
        }

      }
    }
  }
}
