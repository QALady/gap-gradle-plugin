package com.gap.gradle.plugins.gapcookbook

import com.gap.gradle.chef.CookbookUploader
import com.gap.gradle.jenkins.JenkinsClient
import org.gradle.api.Project

class PublishCookbookToChefServerTask {
    private Project project

    PublishCookbookToChefServerTask(project){
        this.project = project
    }

    def execute(){
        requireJenkinsConfig()
    }

    def requireJenkinsConfig() {
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
}
