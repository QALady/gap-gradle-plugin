package com.gap.pipeline.tasks

import static matchers.CustomMatchers.sameString
import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when

import com.gap.pipeline.ec.CommanderClient
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Test

class SonarLinkTaskTest {

    @Test
    public void shouldCreateLinkIfTaskIsRunningInContextOfECJob() {
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gap-sonar-runner'
        project.sonarRunner {
            sonarProperties {
                property "sonar.host.url", "http://sonarurl.com/"
                property "sonar.projectKey", "my.org:project-name"
            }
        }

        def commander = mock(CommanderClient)
        when(commander.isRunningInPipeline()).thenReturn(true)


        new SonarLinkTask(project, commander).execute()

        verify(commander).addLinkToUrl("Sonar Dashboard", sameString("http://sonarurl.com/dashboard/index/my.org:project-name"))
    }

    @Test
    public void shouldNotCreateLinkIfTaskIsNotRunningInContextOfECJob() {
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gap-sonar-runner'

        def commander = mock(CommanderClient)
        when(commander.isRunningInPipeline()).thenReturn(false)


        new SonarLinkTask(project, commander).execute()

        verify(commander, never()).addLinkToUrl(any(), any())
    }
}
