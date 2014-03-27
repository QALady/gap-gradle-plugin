package com.gap.pipeline

import com.gap.gradle.tasks.PromoteArtifactsTask
import com.gap.pipeline.tasks.DownloadArtifactsTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static junit.framework.Assert.assertTrue

class PromoteArtifactsIntegrationTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test
    public void shouldUploadAndDownloadSuccessfully () {
        def version = new Date().toTimestamp().toString().replaceAll(/[ :-]/,"")
        def artifactCoordinates = "com.gap.sandbox.integrationtest:testPromoteArtifacts:${version}"
        uploadArtifactsTo(artifactCoordinates)

        def fromCoordinates = artifactCoordinates
        def toCoordinates = "com.gap.sandbox.integrationtest:prodPromotionTest"
        def destinationArtifactoryUrl = "http://artifactory.gapinc.dev/artifactory/local-prod/"
        promoteArtifacts(fromCoordinates, toCoordinates, destinationArtifactoryUrl)

        //verify if the artifacts have been promoted properly by downloading the artifacts from the new location
        def downloadDir = "${temporaryFolder.root.path}/verifypromotion"
        def downloadCoordinates = "${toCoordinates}:${version}"
        downloadArtifacts(destinationArtifactoryUrl, downloadCoordinates, downloadDir)
        ['file1.txt', 'file2.txt'].each { fileName ->
            assertTrue(new File("${downloadDir}/${fileName}".toString()).exists())
        }
    }

    private def promoteArtifacts(fromCoordinates, String toCoordinates, String destinationArtifactoryUrl) {
        temporaryFolder.newFolder("artifactPromotion")
        def project = ProjectBuilder.builder().withProjectDir(new File("${temporaryFolder.root.path}/artifactPromotion")).withName("prodPromotionTest").build()
        project.repositories{
            ivy{
                layout 'maven'
                url "http://artifactory.gapinc.dev/artifactory/local-non-prod"
            }
        }
        project.apply plugin: 'base'
        project.apply plugin: 'gappipeline'
        project.fromCoordinates = fromCoordinates
        project.fromConfiguration = 'archives'
        project.toCoordinates = toCoordinates
        project.toArtifactoryUrl = destinationArtifactoryUrl
        new PromoteArtifactsTask(project).configure()
        project.tasks.findByName("promoteArtifacts").execute()

    }

    private def uploadArtifactsTo(String coordinates) {
        temporaryFolder.newFolder("upload")
        new File("${temporaryFolder.root.path}/upload/build/artifacts").mkdirs()
        temporaryFolder.newFile("upload/build/artifacts/file1.txt")
        temporaryFolder.newFile("upload/build/artifacts/file2.txt")
        def project = ProjectBuilder.builder().withProjectDir(new File("${temporaryFolder.root.path}/upload")).withName("testPromoteArtifacts").build()
        project.apply plugin: 'base'
        project.apply plugin: 'gappipeline'
        project.artifactCoordinates = coordinates
        project.tasks.findByName("uploadBuildArtifacts").execute()
    }

    private void downloadArtifacts(artifactoryUrl, coordinates, toFolder) {
        new File(toFolder).mkdirs()
        def project = ProjectBuilder.builder().withProjectDir(new File(toFolder.toString())).withName("test").build()
        project.repositories{
            ivy{
                layout 'maven'
                url artifactoryUrl
            }
        }
        project.apply plugin: 'gappipeline'
        project.artifactCoordinates = coordinates
        project.artifactConfiguration = 'archives'
        project.destination = toFolder

        new DownloadArtifactsTask(project).configure()
        project.tasks.findByName("downloadArtifacts").execute()
    }

}
