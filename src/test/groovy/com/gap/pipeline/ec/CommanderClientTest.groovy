package com.gap.pipeline.ec
import static junit.framework.Assert.assertEquals
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat
import static org.junit.rules.ExpectedException.none
import static org.mockito.Mockito.*

import com.gap.gradle.utils.ShellCommandException
import com.gap.pipeline.utils.EnvironmentStub
import com.gap.gradle.utils.ShellCommand
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.Mockito

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
        commander.addLink("changelist.logger", jobId)
        verify(mockShellCommand).execute(['ectool', 'setProperty', '/jobs[1254321]/report-urls/changelist', '/commander/jobs/1254321/default/changelist.logger'])
    }

    @Test
    public void getJobId_shouldReturnIdOfECJobInvokingGradle(){
       environmentStub.setValue('COMMANDER_JOBID', "1345555")
       assertEquals("1345555", commander.getJobId())
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
    public void addLinkToUrl_shouldAssumeCurrentJobIdIfNoneGiven(){
        environmentStub.setValue('COMMANDER_JOBID', "99999")
        commander.addLinkToUrl('new Link', 'http://myurl.com')
        verify(mockShellCommand).execute(['ectool', 'setProperty', '/jobs[99999]/report-urls/new Link', 'http://myurl.com'])
    }

    @Test
    public void getRunProcedureUrl_shouldGiveExpectedFullyQualifiedUrl() {
        def expectedProcedureUrl = "https://commander.gapinc.dev/commander/link/runProcedure/projects/My%20Project/procedures/My%20Procedure?s=Projects"
        assertEquals(expectedProcedureUrl, commander.getRunProcedureUrl("My Project:My Procedure"))
    }

    @Test
    public void addLinkToRunProcedureInJob_shouldLinkToRunProcedureUrlInCurrentJob(){
        environmentStub.setValue('COMMANDER_JOBID', "1345555")
        commander.addLinkToRunProcedureInJob('Run Procedure Link', 'My Project:My Procedure')
        verify(mockShellCommand).execute(['ectool', 'setProperty', '/jobs[1345555]/report-urls/Run Procedure Link', 'https://commander.gapinc.dev/commander/link/runProcedure/projects/My%20Project/procedures/My%20Procedure?s=Projects'])
    }

	@Test
	public void getUserIDFromEC_shouldReturnUserID(){
		commander.getUserId()
		verify(mockShellCommand).execute(['ectool', 'getProperty', '/myJob/launchedByUser'])
	}

	@Test
	public void getUserNameFromEC_shouldReturnUserName(){
		when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/launchedByUser'])).thenReturn('us1n3id')
		when(mockShellCommand.execute(['ectool', 'getProperty', '/users[us1n3id]/fullUserName'])).thenReturn('User Name')
        assertThat(commander.getUserName(), is('User Name'))
    }

	@Test
	public void getStartTimeFromEC_shouldReturnStartJob(){
		commander.getStartTime()
		verify(mockShellCommand).execute(['ectool', 'getProperty', '/myJob/start'])
	}

    @Test
    public void getECProperty_shouldReturnInvalidPropertyIfPropertyDoesNotExist(){
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/idontexist'])).thenThrow(new ShellCommandException("[NoSuchProperty]"))
        assertThat(commander.getECProperty('/myJob/idontexist').isValid(), is(false))
    }

    @Test
    public void getSegmentConfig_ShouldReturnSegmentConfigFromWatchmenConfigECProperties(){
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/configSCMUrl'])).thenReturn('http://svn.gap.com/path/to/repo')
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/workingDir'])).thenReturn('/dev/shm/job_id')
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/ciDir'])).thenReturn('/mnt/electric-commander/workspace/job_id_timestamp')
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/gradleFile'])).thenReturn('segment-name.gradle')
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/config/isManual'])).thenReturn('true')

        SegmentConfig segmentConfig = commander.getCurrentSegmentConfig()

        assertThat(segmentConfig.scmUrl, is('http://svn.gap.com/path/to/repo'))
        assertThat(segmentConfig.workingDir, is('/dev/shm/job_id'))
        assertThat(segmentConfig.ciDir, is('/mnt/electric-commander/workspace/job_id_timestamp'))
        assertThat(segmentConfig.gradleFile, is('segment-name.gradle'))
        assertThat(segmentConfig.isManual, is(true))
    }

    @Test
    public void getSegmentConfig_shouldConfigureAutoTriggeringIfIsManualPropertyDoesNotExist(){
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/configSCMUrl'])).thenReturn('http://svn.gap.com/path/to/repo')
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/workingDir'])).thenReturn('/dev/shm/job_id')
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/ciDir'])).thenReturn('/mnt/electric-commander/workspace/job_id_timestamp')
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/watchmen_config/gradleFile'])).thenReturn('segment-name.gradle')

        SegmentConfig segmentConfig = commander.getCurrentSegmentConfig()

        assertThat(segmentConfig.scmUrl, is('http://svn.gap.com/path/to/repo'))
        assertThat(segmentConfig.workingDir, is('/dev/shm/job_id'))
        assertThat(segmentConfig.ciDir, is('/mnt/electric-commander/workspace/job_id_timestamp'))
        assertThat(segmentConfig.gradleFile, is('segment-name.gradle'))
        assertThat(segmentConfig.isManual, is(false))
    }

    @Test
    public void getCurrentSegment_shouldReturnCurrentlyRunningSegment(){
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/projectName'])).thenReturn('Project Name')
        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/liveProcedure'])).thenReturn('Procedure Name')

        def segment = commander.getCurrentSegment()

        assertThat(segment.projectName, is('Project Name'))
        assertThat(segment.procedureName, is('Procedure Name'))
    }

    @Test
    public void isRunningInPipeline_shouldReturnFalseIfCommanderJobIdPropertyDoesNotExist(){
        def result = commander.isRunningInPipeline()
        assertThat(result, is(false))
    }

    @Test
    public void isRunningInPipeline_shouldReturnTrueIfCommanderJobIdPropertyExists(){
        environmentStub.setValue('COMMANDER_JOBID', "12345")
        def result = commander.isRunningInPipeline()
        assertThat(result, is(true))
    }


}
