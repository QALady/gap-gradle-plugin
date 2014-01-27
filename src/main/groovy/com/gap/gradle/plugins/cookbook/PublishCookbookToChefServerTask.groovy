package com.gap.gradle.plugins.cookbook
import com.gap.gradle.chef.CookbookUploader
import com.gap.gradle.chef.CookbookUtil
import com.gap.gradle.jenkins.JenkinsClient
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

class PublishCookbookToChefServerTask {

    private Project project
    def log = LogFactory.getLog(PublishCookbookToChefServerTask)


    PublishCookbookToChefServerTask(project){
        this.project = project
    }

    def execute() {
        requireJenkinsConfig()
        publishCookbookToChefServer()
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

    def publishCookbookToChefServer() {
        def jenkinsConfig = project.jenkins
        JenkinsClient client = new JenkinsClient(jenkinsConfig.serverUrl, jenkinsConfig.user, jenkinsConfig.authToken)
        CookbookUploader uploader = new CookbookUploader(client)
        def cookbookUtil = new CookbookUtil()
        def cookbookMetadata = cookbookUtil.metadataFrom()
        def cookbookName = project.chef.cookbookName ?: cookbookMetadata.name

        if (!cookbookUtil.doesCookbookExist(cookbookMetadata)) {
            uploader.upload(cookbookName, project.chef.environment)
        }
        else {
            log.info("Skipping triggering of jenkins job as cookbook ${cookbookName} with version ${cookbookMetadata.version} already exists ")
        }
    }


}
