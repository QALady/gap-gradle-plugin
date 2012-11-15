package com.gap.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.Zip

class GapUploadPlugin implements Plugin<Project>{

 
  void apply(Project project) {
  
    def ivyUrl      = project.hasProperty('ivyPublishUrl')      ? project.getProperty('ivyPublishUrl')      : "${project.gradle.gradleUserHomeDir}/local-repo";
    def ivyUsername = project.hasProperty('ivyPublishUsername') ? project.getProperty('ivyPublishUsername') : ""
    def ivyPassword = project.hasProperty('ivyPublishPassword') ? project.getProperty('ivyPublishPassword') : ""

    if (project.plugins.hasPlugin('java')) {

      project.tasks.add(name:'packageSources', type: Jar)
      {
        classifier = 'sources'
        from project.sourceSets.main.allSource
      };
      
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

      project.tasks.add(name:'packageIntegrationTests', type: Zip)
      {
        classifier = 'integrationTests'
        from "src/test/functional"
      }

      project.configure(project){
        uploadArchives {

          dependsOn('uploadSources','uploadTestRuntime', 'uploadConfig', 'uploadIntegrationTest')

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
          sources
          testRuntime{visible true}
          integrationTest
        }

        artifacts {
          sources(packageSources){ type = 'source' }
          config(packageConfigs){ type = 'config' }
          testRuntime(packageTests){ type = 'test' }
          integrationTest(packageIntegrationTests){ type = 'integrationTest' }
        }

        uploadSources {
          repositories { uploadArchives.repositories.each{add it}} //Use same repos as uploadArchives
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

            }
        }

    }
}
