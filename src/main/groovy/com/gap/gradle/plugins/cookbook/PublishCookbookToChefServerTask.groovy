package com.gap.gradle.plugins.cookbook

import com.gap.gradle.chef.CookbookUploader
import com.gap.gradle.chef.CookbookUtil
import com.gap.gradle.jenkins.JenkinsClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

@RequiredParameters([
@Require(parameter = 'jenkins.cookbookServerUrl', description = "Jenkins Server URL for cookbook promotion."),
@Require(parameter = 'jenkins.cookbookUser', description = "Jenkins User ID to trigger job"),
@Require(parameter = 'jenkins.cookbookAuthToken', description = "Jenkins API Auth token to trigger job."),
@Require(parameter = 'chef.metadata', description = "Metadata of cookbook to upload.")
])
class PublishCookbookToChefServerTask extends WatchmenTask  {

    private Project project
    private Log log = LogFactory.getLog(PublishCookbookToChefServerTask)

    PublishCookbookToChefServerTask(project) {
        super(project)
        this.project = project
    }

    def execute() {
        super.validate()
        publishCookbookToChefServer()
    }

    def publishCookbookToChefServer() {
        def jenkinsConfig = project.jenkins
        JenkinsClient client = new JenkinsClient(jenkinsConfig.cookbookServerUrl, jenkinsConfig.cookbookUser, jenkinsConfig.cookbookAuthToken)
        CookbookUploader uploader = new CookbookUploader(client)
        def cookbookUtil = new CookbookUtil()
        def cookbookMetadata = project.chef.metadata
        if (cookbookUtil.doesCookbookExist(cookbookMetadata)) {
            log.info("Skipping triggering of jenkins job as cookbook ${cookbookMetadata.name} with version ${cookbookMetadata.version} already exists")
        } else {
            uploader.upload(project.chef.environment, cookbookMetadata)

        }
    }
}
