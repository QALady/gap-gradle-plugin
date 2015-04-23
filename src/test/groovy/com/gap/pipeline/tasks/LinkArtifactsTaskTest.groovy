package com.gap.pipeline.tasks

import com.gap.gradle.plugins.mobile.CommandRunner
import com.gap.pipeline.ec.CommanderClient
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Before
import org.junit.Test

import static org.mockito.Mockito.*
import static org.testng.Assert.assertEquals

class LinkArtifactsTaskTest {

    private Project project
    private LinkArtifactsTask task
    private CommanderClient commanderClientMock
    private CommandRunner commandRunnerMock

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        project.artifactLocation = "artifacts"

        commanderClientMock = mock(CommanderClient)
        when(commanderClientMock.getCurrentJobDir()).thenReturn("/tmp")
        when(commanderClientMock.getJobId()).thenReturn("1")

        commandRunnerMock = mock(CommandRunner)

        task = new LinkArtifactsTask(project, commanderClientMock)
        task.commanderClient = commanderClientMock
        task.commandRunner = commandRunnerMock
    }

    @Test
    public void shouldCopyArtifacts() throws Exception {
        task.copyArtifacts()

        verify(commandRunnerMock).run("cp", "-R", "artifacts", "/tmp/artifacts")
    }

    @Test
    public void shouldCreateHtmlIndex() throws Exception {
        def baseDir = new File("/tmp/artifacts")
        def dirHtml = new File(baseDir, "dir.html")

        def jsoupMock = new MockFor(Jsoup)

        jsoupMock.demand.parse { file, charset ->
            assertEquals(file, dirHtml)
            assertEquals(charset, "UTF-8")

            return new Document("")
        }

        jsoupMock.use {
            task.createHtmlIndex()
        }

        verify(commandRunnerMock).run(baseDir, "tree", "--dirsfirst", "-CF", "-o", "dir.html", "-H", ".", "-L", "1", "-T", baseDir, "-I", "dir.html")
    }

    @Test
    public void shouldLinkArtifacts() throws Exception {
        task.linkArtifacts()

        verify(commanderClientMock).addLink("/tmp/artifacts/dir.html", "1")
    }

}
