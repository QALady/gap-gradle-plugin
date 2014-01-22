package com.gap.gradle.chef
import com.gap.gradle.jenkins.JenkinsClient
import org.junit.Ignore
import org.junit.Test

class CookbooksUploaderIntegrationTest {

    @Test
    void shouldUploadRefAppCookbookUsingJenkinsPipeline() {
        def jenkins = new JenkinsClient("http://chefci.phx.gapinc.dev:8080", "em4l5d0", "4661bb66b1f850bdff9c3ce5f5daca65")
        def uploader = new CookbookUploader(jenkins, 5000)
        uploader.upload("ref-app", "tdev")
    }
}
