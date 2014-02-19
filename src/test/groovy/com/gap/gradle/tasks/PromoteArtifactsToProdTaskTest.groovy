package com.gap.gradle.tasks
import static junit.framework.Assert.assertTrue

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.exception.MissingParameterException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder

class PromoteArtifactsToProdTaskTest {
    private Project project
    private PromoteArtifactsToProdTask task

    @Before
    void setUp() {
        project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build()
        project.apply plugin: 'gapproddeploy'
        project.artifactCoordinates = 'com.gap.sandbox:testDownload:123'
        project.toArtifactCoordinates = 'com.gap.sandbox:testDownload'

        project.destination = 'downloads'
        project.artifactConfiguration = 'archives'
        task = new PromoteArtifactsToProdTask(project)
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
    @Ignore
    void shouldThrownException_whenArtifactCoordinatesAreNotProvided() {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'artifactCoordinates'")
        project.artifactCoordinates=null
        task.validate()
    }

    @Test
    @Ignore
    void shouldThrownException_whenArtifactConfigurationNotProvided() {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'artifactConfiguration'")
        project.artifactConfiguration=null
        task.validate()
    }

    @Test
    @Ignore
    void shouldThrownException_whenDestinationArtifactCoordinatesAreNotProvided() {
        exception.expect(MissingParameterException)
        exception.expectMessage("Missing required parameter: 'toArtifactCoordinates'")
        project.toArtifactCoordinates=null
        task.validate()
    }

}
