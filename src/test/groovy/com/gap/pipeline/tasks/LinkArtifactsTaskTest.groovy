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

    private static final File BASE_DIR = new File("/tmp/artifacts")
    private static final String OUTPUT_FILE_NAME = "build-artifacts.html"

    private Project project
    private LinkArtifactsTask task
    private CommanderClient commanderClientMock
    private CommandRunner commandRunnerMock

    @Before
    void setUp() {
        project = ProjectBuilder.builder().build()
        project.metaClass.artifactLocation = "artifacts"

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
        def fakeOutputFile = new File(BASE_DIR, OUTPUT_FILE_NAME)
        fakeOutputFile.parentFile.mkdirs()
        fakeOutputFile.createNewFile()

        def jsoupMock = new MockFor(Jsoup)

        jsoupMock.demand.parse { file, charset ->
            assertEquals(file, fakeOutputFile)
            assertEquals(charset, "UTF-8")

            return new Document("")
        }

        jsoupMock.use {
            task.createHtmlIndex()
        }

        verify(commandRunnerMock).run(BASE_DIR, "tree", "--dirsfirst", "-CF", "-o", OUTPUT_FILE_NAME, "-H", ".", "-L", "1", "-T", BASE_DIR, "-I", OUTPUT_FILE_NAME)
    }

    @Test
    public void shouldLinkArtifacts() throws Exception {
        task.linkArtifacts()

        verify(commanderClientMock).addLink(OUTPUT_FILE_NAME, "1")
    }

}
