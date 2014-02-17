package com.gap.gradle.plugins.cookbook

import groovy.json.JsonSlurper
import org.gradle.api.Project


class PublishCookbookToArtifactoryTask {
    private Project project

    PublishCookbookToArtifactoryTask(Project project){
        this.project = project
    }

    void execute() {
        project.configurations {
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
                url "http://artifactory.gapinc.dev/artifactory/local-non-prod"
                credentials {
                    username 'admin'
                    password 'password'
                }
            }
        }
        generateMetadataJson()
        setProjectVersionFromMetadata()
        project.uploadCookbooks.execute()
    }


    void generateMetadataJson( ) {
        project.exec {
            commandLine 'knife', 'cookbook', 'metadata', 'from', 'file',    '../metadata.rb'
        }
    }

    void setProjectVersionFromMetadata( ) {
        def metadataJson = new File('metadata.json').text
        def json = new JsonSlurper().parseText(metadataJson)
        def jobid =    System.getenv()['COMMANDER_JOBID']
        if (!jobid) jobid = 'local'
        project.version = "${json['version']}.${jobid}"
    }
}
