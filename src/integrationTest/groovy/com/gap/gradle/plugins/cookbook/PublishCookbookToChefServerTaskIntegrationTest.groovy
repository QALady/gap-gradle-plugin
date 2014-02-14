package com.gap.gradle.plugins.cookbook
import org.apache.commons.io.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PublishCookbookToChefServerTaskIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    @Ignore
    void shouldUploadCookbookUsingJenkinsPipeline() {
        def project = ProjectBuilder.builder().build()

        project.apply plugin: 'gapcookbook'
        project.jenkins.cookbookServerUrl = "http://chefci.phx.gapinc.dev:8080"
        project.jenkins.cookbookUser = "em4l5d0"
        project.jenkins.cookbookAuthToken = "4661bb66b1f850bdff9c3ce5f5daca65"
        project.chef.cookbookName = "ref-app"

        def metadataFile = tempFolder.newFile("metadata.rb")
        FileUtils.writeStringToFile(metadataFile, "version '999.99.9999'\n name  'ref-app'")
        project.chef.cookbookDir = tempFolder.root.path

        project.tasks.findByName("generateCookbookMetadata").execute()
        def publishCookbookTask = project.tasks.findByName('publishCookbookToChefServer')
        publishCookbookTask.execute()
    }
}
