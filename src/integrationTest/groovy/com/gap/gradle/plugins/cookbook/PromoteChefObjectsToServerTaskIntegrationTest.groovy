package com.gap.gradle.plugins.cookbook
import org.apache.commons.io.FileUtils
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PromoteChefObjectsToServerTaskIntegrationTest {

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    void shouldPromoteChefObjectsToServerUsingJenkinsPipeline() {
        def project = ProjectBuilder.builder().build()

        project.apply plugin: 'gapcookbook'
        project.jenkins.serverUrl = "http://jenkins01.phx.gapinc.dev:8080"
        project.jenkins.user = "kr8s8k9"
		project.jenkins.jobName = "TagProdReady"
        project.jenkins.authToken = "4661bb66b1f850bdff9c3ce5f5daca65"
		project.parameters.COMMIT_ID = "vijay1"
		project.parameters.TAG_MESSAGE = "bandari"
		project.parameters.parameterNames = "COMMIT_ID,TAG_MESSAGE"
        def publishCookbookTask = project.tasks.findByName('promoteChefObjectsToServer')
        publishCookbookTask.execute()
    }
}
