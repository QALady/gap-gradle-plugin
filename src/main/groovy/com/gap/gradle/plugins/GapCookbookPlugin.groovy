package com.gap.gradle.plugins

import com.gap.gradle.chef.CookbookUploader
import com.gap.gradle.jenkins.JenkinsClient
import groovy.json.JsonSlurper
import org.gradle.api.Plugin
import org.gradle.api.Project
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
        project.extensions.create('jenkins', JenkinsConfig)
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
                ivy {
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

        project.task('publishCookbookToChefServer2') << {
            requireJenkinsConfig(project)
  //          publishCookbookToChefServer(project.jenkins.serverUrl, project.jenkins.user, project.jenkins.authToken)
        }
    }

    def requireJenkinsConfig(project) {
        if (!project.jenkins.serverUrl) {
            throw new Exception("No jenkins url configured")
        } else if (!project.jenkins.user) {
            throw new Exception("No jenkins user configured")
        } else if (!project.jenkins.authToken) {
            throw new Exception("No jenkins auth-token configured")
        }
    }

    def publishCookbookToChefServer(serverUrl, user, authToken) {
        JenkinsClient client = new JenkinsClient(serverUrl, user, authToken)
        CookbookUploader uploader = new CookbookUploader(client)
        uploader.upload(/* TODO */ "cookbook name", /* TODO */ "env")
    }

    void verifyIfCookbookDirectoryIsValid() {
        def current_dir = System.getProperty("user.dir")
        def pattern = ".*/cookbooks/[^/]+"
        if ( !(current_dir ==~ pattern)) {
             throw new Exception("Your current working directory is ${current_dir}. However for uploading to chef server your cookbook should be located in 'cookbooks/<your cookbook name>'")
        }
    }

    void generateMetadataJson(Project project) {
        project.exec {
            commandLine 'knife', 'cookbook', 'metadata', 'from', 'file',    '../metadata.rb'
        }
    }

    void setProjectVersionFromMetadata(Project project) {
        def metadataJson = new File('metadata.json').text
        def json = new JsonSlurper().parseText(metadataJson)
        def cookbookVersion = json['version']
        def jobid =    System.getenv()['COMMANDER_JOBID']
        if (!jobid) jobid = 'local'
        project.version = "${json['version']}.${jobid}"
    }
}
