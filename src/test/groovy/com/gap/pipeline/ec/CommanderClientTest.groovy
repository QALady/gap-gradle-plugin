package com.gap.pipeline.ec

import static junit.framework.Assert.assertEquals
import static org.junit.rules.ExpectedException.none
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

import com.gap.pipeline.utils.ShellCommand
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mockito
import com.gap.pipeline.utils.EnvironmentStub

public class CommanderClientTest {
    @Rule
    public final ExpectedException exception = none()

    def commander
    private ShellCommand mockShellCommand
    private EnvironmentStub environmentStub

    @Before
    void setUp(){
        mockShellCommand = mock(ShellCommand,Mockito.RETURNS_SMART_NULLS)
        environmentStub = new EnvironmentStub()
        commander = new CommanderClient(mockShellCommand,environmentStub)
    }
    @Test
    public void addLink_shouldInvokeEcToolToCreateLinks(){
        def jobId = "1254321"
        commander.addLink("changelist.log", jobId)
        verify(mockShellCommand).execute(['ectool', 'setProperty', '/jobs[1254321]/report-urls/changelist', '/commander/jobs/1254321/default/changelist.log'])
    }

    @Test
    public void getJobId_shouldReturnIdOfECJobInvokingGradle(){
       environmentStub.setValue('COMMANDER_JOBID', "1345555")
       assertEquals("1345555", commander.getJobId())
    }

    @Test
    public void runProcedure_shouldInvokeCommanderProcedure(){
        commander.runProcedure("My Project:My Procedure")
        verify(mockShellCommand).execute(["ectool", "runProcedure",  "My Project", "--procedureName", "My Procedure"])
    }

    @Test
    public void runProcedure_shouldInvokeCommanderProcedureWithArguments(){
        commander.runProcedure('My Project:My Procedure', [artifactLocation: 'com.gap.sandbox:iso:1234', param2: 'value2'])
        verify(mockShellCommand).execute(["ectool", "runProcedure",  "My Project", "--procedureName", "My Procedure", "--actualParameter", "artifactLocation=com.gap.sandbox:iso:1234", "param2=value2"])
    }

    @Test
    public void runProcedure_shouldThrowException_whenProcedureNameIsNotInRightFormat() {
        exception.expect(IllegalArgumentException)
        exception.expectMessage("The procedure name 'My ProjectMy Procedure' is invalid. It should be of the format '<project name>:<procedure name>")
        commander.runProcedure("My ProjectMy Procedure")
     }

    @Test
    public void getCurrentJobDir_shouldReturnDirectoryOfCurrentJob(){
        environmentStub.setValue('COMMANDER_WORKSPACE_UNIX', '/workspace/path/dirName')
        assertEquals("/workspace/path/dirName", commander.getCurrentJobDir())
    }

    @Test
    public void setDefaultParameterValue_shouldInvokeEcToolWithExpectedParameters() {
        commander.setDefaultParameterValue('My Project:My Procedure', 'param1',  'value:1:1')
        verify(mockShellCommand).execute(["ectool", "modifyFormalParameter",  "My Project", "My Procedure", "param1", "--defaultValue", 'value:1:1'])
    }

    @Test
    public void setDefaultParameterValue_shouldThrowException_whenProcedureNameIsNotInRightFormat() {
        exception.expect(IllegalArgumentException)
        exception.expectMessage("The procedure name 'My ProjectMy Procedure' is invalid. It should be of the format '<project name>:<procedure name>")
        commander.setDefaultParameterValue("My ProjectMy Procedure", 'name', 'value')
    }

    @Test
    public void addLinkToUrl_shouldAddLinkInJobToGivenUrl(){
        commander.addLinkToUrl('new Link', 'http://myurl.com', '1254321')
        verify(mockShellCommand).execute(['ectool', 'setProperty', '/jobs[1254321]/report-urls/new Link', 'http://myurl.com'])
    }

    @Test
    public void getRunProcedureUrl_shouldGiveExpectedFullyQualifiedUrl() {
        def expectedProcedureUrl = "https://commander.phx.gapinc.dev/commander/link/runProcedure/projects/My%20Project/procedures/My%20Procedure?s=Projects"
        assertEquals(expectedProcedureUrl, commander.getRunProcedureUrl("My Project:My Procedure"))
    }

    @Test
    public void addLinkToRunProcedureInJob_shouldLinkToRunProcedureUrlInCurrentJob(){
        environmentStub.setValue('COMMANDER_JOBID', "1345555")
        commander.addLinkToRunProcedureInJob('Run Procedure Link', 'My Project:My Procedure')
        verify(mockShellCommand).execute(['ectool', 'setProperty', '/jobs[1345555]/report-urls/Run Procedure Link', 'https://commander.phx.gapinc.dev/commander/link/runProcedure/projects/My%20Project/procedures/My%20Procedure?s=Projects'])
    }

	@Test
	public void getUserIDFromEC_shouldReturnUserID(){
		commander.getUserId()
		verify(mockShellCommand).execute(['ectool', 'getProperty', '/myJob/launchedByUser'])
	}

	@Test
	public void getUserNameFromEC_shouldReturnUserName(){
		commander.getUserName()
		verify(mockShellCommand).execute(['ectool', 'getProperty', '/myJob/launchedByUser'])
	}

	@Test
	public void getStartTimeFromEC_shouldReturnStartJob(){
		commander.getStartTime()
		verify(mockShellCommand).execute(['ectool', 'getProperty', '/myJob/start/'])
	}
}