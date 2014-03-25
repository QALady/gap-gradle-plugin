package com.gap.gradle.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.exception.MissingParameterException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder

import static junit.framework.Assert.assertEquals
import static junit.framework.Assert.assertTrue

class PromoteArtifactsTaskTest {
    private Project project
    private PromoteArtifactsTask task

    @Before
    void setUp() {
        project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build()
        project.fromCoordinates = 'com.gap.sandbox:testDownload:123'
        project.toCoordinates = 'com.gap.sandbox:prod'
        project.fromConfiguration = 'archives'
        project.toArtifactoryUrl = 'http://www.artifactory.url/prod'
        project.ivy = [:]
        task = new PromoteArtifactsTask(project)
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Rule
    public ExpectedException exception = ExpectedException.none()

    @Test
    void shouldSuccessfullyPromoteArtifacts() {
        project.task('downloadArtifacts') << {
            new ShellCommand().execute("touch ${project.rootDir}/downloads/file1.txt")
            new ShellCommand().execute("touch ${project.rootDir}/downloads/file2.txt")
        }

        def uploadArtifactsWasCalled = false
        project.task('uploadBuildArtifacts') << {
            uploadArtifactsWasCalled = true
        }
        task.execute()

        ["${project.rootDir}/build/artifacts/file1.txt", "${project.rootDir}/build/artifacts/file2.txt"].each{ file ->
            assertTrue("Cannot find ${file}", new File(file).exists())
        }
        assertTrue("uploadBuildArtifacts task was not invoked.", uploadArtifactsWasCalled)
    }

    @Test
    void shouldDownloadTheArtifactsFromTheRightLocation(){
        def downloadCoordinates = null
        def downloadConfiguration = null
        def downloaddir = null
        project.fromCoordinates = 'com.gap.sandbox:testDownload:201'
        project.fromConfiguration = 'myconfig'
        project.task('downloadArtifacts') << {
            downloadConfiguration = project.artifactConfiguration
            downloadCoordinates = project.artifactCoordinates
            downloaddir = project.destination
        }

        project.task('uploadBuildArtifacts') << {  }
        task.execute()

        assertEquals("com.gap.sandbox:testDownload:201", downloadCoordinates)
        assertEquals("myconfig", downloadConfiguration)
        assertEquals("${project.rootDir}/downloads", downloaddir)
    }

    @Test
    void shouldUploadTheArtifactsToTheRightLocation() {
        def uploadCoordinates = null
        def destinationIvyUrl = null
        project.fromCoordinates = 'com.gap.sandbox:testDownload:201'
        project.toCoordinates = 'com.gap.mysandbox:prod'
        project.toArtifactoryUrl = 'http://www.artifactory.url/prod'

        project.task('downloadArtifacts') << {}

        project.task('uploadBuildArtifacts') << {
            uploadCoordinates = project.artifactCoordinates
            destinationIvyUrl = project.ivy.url
        }

        task.execute()

        assertEquals("com.gap.mysandbox:prod:201", uploadCoordinates)
    }

    @Test
    void shouldThrownException_whenSourceArtifactCoordinatesAreNotProvided() {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'fromCoordinates'")
        project.fromCoordinates=null
        task.validate()
    }

    @Test
    void shouldThrownException_whenSourceArtifactConfigurationNotProvided() {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'fromConfiguration'")
        project.fromConfiguration=null
        task.validate()
    }

    @Test
    void shouldThrownException_whenDestinationArtifactCoordinatesAreNotProvided() {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'toCoordinates'")
        project.toCoordinates=null
        task.validate()
    }

    @Test
    void shouldThrownException_whenDestinationArtifactoryUrlIsNotProvided() {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'toArtifactoryUrl'")
        project.toArtifactoryUrl=null
        task.validate()
    }

}
