package com.gap.gradle.tasks
import static junit.framework.Assert.assertTrue

import com.gap.gradle.utils.ShellCommand
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.tooling.model.GradleTask
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class PromoteArtifactsToProdTaskTest {
    private Project project
    private GradleTask gradleTask

    @Before
    void setUp() {
        project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build()
        project.apply plugin: 'gapproddeploy'
        project.artifactCoordinates = 'com.gap.sandbox:testDownload:123'
        project.destination = 'downloads'
        project.artifactConfiguration = 'archives'
    }

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

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
        def task = new PromoteArtifactsToProdTask(project)
        task.execute()

        ["${project.rootDir}/build/artifacts/file1.txt", "${project.rootDir}/build/artifacts/file2.txt"].each{ file ->
            assertTrue("Cannot find file with name ${file}", new File(file).exists())
        }
        assertTrue("uploadBuildArtifacts task was not invoked.", uploadArtifactsWasCalled)
    }
}
