package com.gap.gradle.plugins.artifacts

import static junit.framework.Assert.assertTrue

import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import com.gap.pipeline.tasks.DownloadArtifactsTask

class ArtifactsUploadDownloadIntegrationTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Test
    public void shouldUploadAndDownloadSuccessfully () {
        def version = new Date().toTimestamp().toString().replaceAll(/[ :-]/,"")
        def artifactCoordinates = "com.gap.sandbox.integrationtest:test:${version}"
        uploadArtifactsTo(artifactCoordinates)
        downloadArtifactsFrom(artifactCoordinates)
        def downloadLocation = "${temporaryFolder.root.path}/download/destination"
        ['file1.txt', 'file2.txt'].each { fileName ->
            assertTrue(new File("${downloadLocation}/${fileName}".toString()).exists())
        }
    }

    private void downloadArtifactsFrom(String coordinates) {
        temporaryFolder.newFolder("download")
        def project = ProjectBuilder.builder().withProjectDir(new File("${temporaryFolder.root.path}/download".toString())).withName("test").build()
        project.repositories{
            ivy{
                layout 'maven'
                url "http://artifactory.gapinc.dev/artifactory/local-non-prod"
            }
        }
        project.apply plugin: 'gappipeline'
        project.metaClass.artifactCoordinates = coordinates
        project.metaClass.artifactConfiguration = 'archives'
        project.metaClass.destination = 'destination'

        new DownloadArtifactsTask(project).configure()
        project.tasks.findByName("downloadArtifacts").execute()
    }

    private void uploadArtifactsTo(String coordinates) {
        temporaryFolder.newFolder("upload")
        new File("${temporaryFolder.root.path}/upload/build/artifacts").mkdirs()
        temporaryFolder.newFile("upload/build/artifacts/file1.txt")
        temporaryFolder.newFile("upload/build/artifacts/file2.txt")
        def project = ProjectBuilder.builder().withProjectDir(new File("${temporaryFolder.root.path}/upload")).withName("test").build()
        project.apply plugin: 'base'
        project.apply plugin: 'gappipeline'
        project.metaClass.artifactCoordinates = coordinates
        project.ivy.checkIfExists = true;
        project.tasks.findByName("uploadBuildArtifacts").execute()
    }

}
