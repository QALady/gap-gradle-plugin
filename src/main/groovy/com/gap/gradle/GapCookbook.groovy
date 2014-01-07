package com.gap.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import groovy.json.JsonSlurper

/*
  Defines the following tasks
  publishCookbookToArtifactory - publishes cookbook metadata information to the ivy repository
  publishCookbookToChefServer - uploads the cookbook to the chef server

  usage

  gradle publishCookbookToArtifactory
  gradle publishCookbookToChefServer
*/
class GapCookbookPlugin implements Plugin<Project> {
  void apply(Project project) {

    project.task('publishCookbookToArtifactory') << {

      project.configurations{
        cookbooks
      }
      project.artifacts {
        cookbooks new File('git_sha1.txt')
        cookbooks new File('metadata.rb')
        cookbooks new File('metadata.json')
      }
      project.uploadCookbooks.repositories {
        ivy{
            layout 'maven'
            url "http://repo1.phx.gapinc.dev/artifactory/libs-release-local"
            credentials {
              username 'admin'
              password 'password'
            }
          }
      }

      generateMetadataJson(project)
      setProjectVersionFromMetadata(project)
      project.uploadCookbooks.execute()
    }

    project.task('publishCookbookToChefServer') <<{

      verifyIfCookbookDirectoryIsValid()

      def home_dir = System.getenv()['HOME']
      def current_dir = System.getProperty("user.dir")
      def knife_push_working_dir = "${current_dir}/../.."
      project.exec {
        workingDir knife_push_working_dir
        commandLine "${home_dir}/knife/push.rb", '.'
      }
    }
  }

  void verifyIfCookbookDirectoryIsValid()
  {
    def current_dir = System.getProperty("user.dir")
    def pattern = ".*/cookbooks/[^/]+"
    if ( !(current_dir ==~ pattern))
       throw new Exception("Your current working directory is ${current_dir}. However for uploading to chef server your cookbook should be located in 'cookbooks/<your cookbook name>'")
  }

  void generateMetadataJson(Project project){
    project.exec {
      commandLine 'knife', 'cookbook', 'metadata', 'from', 'file',  '../metadata.rb'
    }
  }

  void setProjectVersionFromMetadata(Project project){
    def metadataJson = new File('metadata.json').text
    def json = new JsonSlurper().parseText(metadataJson)
    def cookbookVersion = json['version']
    def jobid =  System.getenv()['COMMANDER_JOBID']
    if (!jobid) jobid = 'local'
    project.version = "${json['version']}.${jobid}"
  }
}
