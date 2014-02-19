package com.gap.pipeline.ec

import static junit.framework.Assert.assertTrue
import static org.mockito.Mockito.*

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CommanderArtifactsTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    void shouldProvideLinksToArtifactsInCurrentJob(){
        def mockClient = mock(CommanderClient)
        temporaryFolder.newFolder("artifacts")
        temporaryFolder.newFile("artifacts/changelist.log")
        temporaryFolder.newFile("artifacts/someArtifact.log")
        when(mockClient.currentJobDir).thenReturn(temporaryFolder.root.path)
        when(mockClient.getJobId()).thenReturn("job id")

        new CommanderArtifacts(mockClient).publishLinks()

        verify(mockClient).addLink("changelist.log", "job id")
        verify(mockClient).addLink("someArtifact.log", "job id")
    }

    @Test
    void shouldCopyFileToArtifactLocation(){
        temporaryFolder.newFolder("source")
        def artifact = temporaryFolder.newFile("source/artifact.rpm")
        def mockClient = mock(CommanderClient)
        when(mockClient.currentJobDir).thenReturn(temporaryFolder.root.path)
        new CommanderArtifacts(mockClient).copyToArtifactsDir(artifact.path)
        assertTrue(new File(temporaryFolder.root.path + "/artifacts/artifact.rpm").exists())
    }
}
