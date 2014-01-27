package com.gap.gradle.plugins.cookbook

import com.gap.gradle.chef.CookbookUtil
import groovy.mock.interceptor.MockFor
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

        def mockCookbookUtil = new MockFor(CookbookUtil)
        mockCookbookUtil.demand.metadataFrom { path ->
            [ name: "myapp", version: "1.1.13" ]
        }
        mockCookbookUtil.demand.doesCookbookExist {return false}

        mockCookbookUtil.use{
            def publishCookbookTask = project.tasks.findByName('publishCookbookToChefServer')
            publishCookbookTask.execute()
        }
    }
}
