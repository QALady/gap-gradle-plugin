package com.gap.gradle.tasks

import com.gap.gradle.utils.GradleTask
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test;
import static org.mockito.Mockito.*

class PromoteArtifactsToProdTaskTest {

    @Test
    void shouldInvokeDownloadArtifactsToATemporaryDownloadFolder () {
        def project = ProjectBuilder.builder().build();
        project.apply plugin: 'gapproddeploy'
        project.apply plugin: 'gappipeline'

        def gradleTask = mock(GradleTask)
        project.artifactCoordinates = 'com.gap.sandbox:testDownload:123'
        project.destination = 'downloads'
        project.artifactConfiguration = 'archives'

        def task = new PromoteArtifactsToProdTask(project,gradleTask)
        task.execute()

        verify(gradleTask).execute(project, 'downloadArtifacts')

    }
}
