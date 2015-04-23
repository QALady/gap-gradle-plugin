package com.gap.pipeline.tasks

import com.gap.gradle.utils.ShellCommand
import org.junit.Before
import org.junit.Test
import org.powermock.api.mockito.PowerMockito
import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.any
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.never
import static org.mockito.Mockito.verify
import static org.mockito.Mockito.when
import static org.mockito.Mockito.doNothing
import org.mockito.Mockito
import org.junit.rules.TemporaryFolder
import org.junit.Rule
import org.powermock.modules.junit4.PowerMockRunner
import org.powermock.core.classloader.annotations.PrepareForTest
import org.junit.runner.RunWith
import com.gap.pipeline.ec.CommanderClient
import org.gradle.testfixtures.ProjectBuilder


@RunWith(PowerMockRunner.class)
@PrepareForTest(LinkArtifactsTask)
 class LinkArtifactsTaskTest {

    @Rule
    public TemporaryFolder artifacts = new TemporaryFolder()




	@Before
    void setUp(){
       
    }

    @Test
    public void shouldLinkArtifacts() {
        def project = ProjectBuilder.builder().build()
        def commander = mock(CommanderClient)
        when(commander.getCurrentJobDir()).thenReturn("TestArtifacts")
        def mockShellCommand = mock(ShellCommand)

        when(mockShellCommand.execute(anyString())).thenReturn("test")





        PowerMockito.whenNew(ShellCommand).withNoArguments().thenReturn(mockShellCommand)
        PowerMockito.whenNew(CommanderClient).withNoArguments().thenReturn(commander)
        //when(mockShellCommand.execute(["cp", "-R",  "artifacts", "TestArtifacts/artifacts"])).thenReturn("")
        project.apply plugin: 'gappipeline'
        project.artifactLocation = "artifacts"
        //LinkArtifactsTask linkArtifactsTask = new LinkArtifactsTask(project)
        //Mockito.doNothing().when(linkArtifactsTask).copyArtifacts()
       

        project.tasks.findByName('linkArtifacts').execute()
        //verify(mockShellCommand).execute(["cp","-R","artifacts","TestArtifacts/artifacts"])
        
        //verify(commander).addLinkToUrl("Sonar Dashboard", sameString("http://sonarurl.com/dashboard/index/my.org:project-name"))
    }

}
