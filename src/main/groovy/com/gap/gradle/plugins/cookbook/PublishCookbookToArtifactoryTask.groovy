package com.gap.gradle.plugins.cookbook

import groovy.json.JsonSlurper
import org.gradle.api.Project
import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient

class PublishCookbookToArtifactoryTask {
    private Project project
    def shellCommand
    private def commanderClient

    PublishCookbookToArtifactoryTask(Project project){
        this.project = project
        this.shellCommand = new ShellCommand()
        this.commanderClient = new CommanderClient()
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
                    username 'ec-build-snap'
                    password '$nap4me'
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
        def timeStamp =  "local"
        if(!isLocal()) {
          def today= new Date()
          timeStamp = today.format("yyyyMMddHHmmss")
        }
        project.version = "${json['version']}.${timeStamp}"
        commanderClient.setECProperty("/myJob/version", project.version)
    }

    private boolean isLocal() {
        !commanderClient.isRunningInPipeline()
    }
}
