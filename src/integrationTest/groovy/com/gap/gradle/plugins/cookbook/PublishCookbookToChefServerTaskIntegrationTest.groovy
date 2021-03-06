package com.gap.gradle.plugins.cookbook

import org.apache.commons.io.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PublishCookbookToChefServerTaskIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder()

    @Test
    void shouldUploadCookbookUsingJenkinsPipeline() {
        def project = ProjectBuilder.builder().build()

        project.apply plugin: 'gapcookbook'
        project.jenkins.cookbookServerUrl = "http://dgphxaciap004.phx.gapinc.dev:8080/"
        project.jenkins.cookbookUser = "integtest"
        project.jenkins.cookbookAuthToken = "15497c7c234c5940a3573672363e692c"
        project.chef.cookbookName = "ref-app"

        def metadataFile = tempFolder.newFile("metadata.rb")
        FileUtils.writeStringToFile(metadataFile, "version '999.99.9999'\n name  'ref-app'")
        project.chef.cookbookDir = tempFolder.root.path

        project.tasks.findByName("generateCookbookMetadata").execute()

        def publishCookbookTask = project.tasks.findByName('publishCookbookToChefServer')
        publishCookbookTask.execute()
    }
}
