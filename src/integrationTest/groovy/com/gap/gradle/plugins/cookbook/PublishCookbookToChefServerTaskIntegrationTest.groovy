package com.gap.gradle.plugins.cookbook

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class PublishCookbookToChefServerTaskIntegrationTest {

    @Test
    void shouldUploadCookbookUsingJenkinsPipeline() {
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        project.jenkins.serverUrl = "http://chefci.phx.gapinc.dev:8080"
        project.jenkins.user = "em4l5d0"
        project.jenkins.authToken = "4661bb66b1f850bdff9c3ce5f5daca65"
        project.chef.cookbookName = "ref-app"

        def publishCookbookTask = project.tasks.findByName('publishCookbookToChefServer2')

        publishCookbookTask.execute()
    }
}
