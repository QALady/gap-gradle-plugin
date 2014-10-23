package com.gap.pipeline.ec
import static junit.framework.Assert.assertEquals
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertEquals
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

    CommanderClient commander
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

    @Test
    public void getCredential_shouldInvokeEcToolToGetFullCredential() {
        environmentStub.setValue('COMMANDER_JOBID', '12345')

        def credentialName = 'myCredential'
        def valueName = 'userName'
        commander.getCredential(credentialName, valueName)

        verify(mockShellCommand).execute(['ectool', 'getFullCredential', credentialName, '--value', valueName])
    }

    @Test
    public void getArtifactoryUserName_shouldInvokeEcToolToGetFullCredential() {
        environmentStub.setValue('COMMANDER_JOBID', '12345')

        commander.getArtifactoryUserName()

        verify(mockShellCommand).execute(['ectool', 'getFullCredential', '/projects/WM Credentials/credentials/WMArtifactory', '--value', 'userName'])
    }

    @Test
    public void getArtifactoryPassword_shouldInvokeEcToolToGetFullCredential() {
        environmentStub.setValue('COMMANDER_JOBID', '12345')

        commander.getArtifactoryPassword()

        verify(mockShellCommand).execute(['ectool', 'getFullCredential', '/projects/WM Credentials/credentials/WMArtifactory', '--value', 'password'])
    }

    @Test
    void shouldParseJson()
    {
        def expectedArray = [2413, 2414, 2415, 2416, 2417, 2418, 2419, 2420]
        String[] propList= commander.parseJson(json01)
        assertEquals(expectedArray,propList)


    }

    def json01="""{
   "requestId" : "1",
   "propertySheet" : {
      "owner" : "project: WM Segment-1.90",
      "modifyTime" : "2014-10-04T03:33:10.819Z",
      "propertySheetId" : "0537876e-0de3-11e4-8189-00505625f614",
      "lastModifiedBy" : "project: WM Segment-2.2",
      "property" : [
         {
            "owner" : "project: WM Segment-1.95",
            "propertyId" : "73789fea-193a-11e4-86d4-00505625f614",
            "propertyName" : "2413",
            "propertySheetId" : "73789feb-193a-11e4-86d4-00505625f614",
            "modifyTime" : "2014-08-01T05:12:36.189Z",
            "lastModifiedBy" : "project: WM Segment-1.95",
            "createTime" : "2014-08-01T05:12:36.189Z"
         },
         {
            "owner" : "project: WM Segment-1.95",
            "propertyId" : "93481e08-19a6-11e4-9e8b-00505625f614",
            "propertyName" : "2414",
            "propertySheetId" : "93484519-19a6-11e4-9e8b-00505625f614",
            "modifyTime" : "2014-08-01T18:06:35.275Z",
            "lastModifiedBy" : "project: WM Segment-1.95",
            "createTime" : "2014-08-01T18:06:35.275Z"
         },
         {
            "owner" : "project: WM Segment-1.95",
            "propertyId" : "0dcae74d-1a04-11e4-a348-00505625f614",
            "propertyName" : "2415",
            "propertySheetId" : "0dcae74e-1a04-11e4-a348-00505625f614",
            "modifyTime" : "2014-08-02T05:15:43.965Z",
            "lastModifiedBy" : "project: WM Segment-1.95",
            "createTime" : "2014-08-02T05:15:43.965Z"
         },
         {
            "owner" : "project: WM Segment-1.95",
            "propertyId" : "eb5d544a-1acc-11e4-bcfa-00505625f614",
            "propertyName" : "2416",
            "propertySheetId" : "eb5d544b-1acc-11e4-bcfa-00505625f614",
            "modifyTime" : "2014-08-03T05:13:35.143Z",
            "lastModifiedBy" : "project: WM Segment-1.95",
            "createTime" : "2014-08-03T05:13:35.143Z"
         },
         {
            "owner" : "project: WM Segment-1.95",
            "propertyId" : "6a4be2d3-1b96-11e4-8189-00505625f614",
            "propertyName" : "2417",
            "propertySheetId" : "6a4be2d4-1b96-11e4-8189-00505625f614",
            "modifyTime" : "2014-08-04T05:15:56.899Z",
            "lastModifiedBy" : "project: WM Segment-1.95",
            "createTime" : "2014-08-04T05:15:56.899Z"
         },
         {
            "owner" : "project: WM Segment-1.96",
            "propertyId" : "db296db1-1d28-11e4-8ee0-00505625f614",
            "propertyName" : "2418",
            "propertySheetId" : "db296db2-1d28-11e4-8ee0-00505625f614",
            "modifyTime" : "2014-08-06T05:16:43.917Z",
            "lastModifiedBy" : "project: WM Segment-1.96",
            "createTime" : "2014-08-06T05:16:43.917Z"
         },
         {
            "owner" : "project: WM Segment-1.96",
            "propertyId" : "b9730c47-1e4c-11e4-bcd3-00505625f614",
            "propertyName" : "2419",
            "propertySheetId" : "b9730c48-1e4c-11e4-bcd3-00505625f614",
            "modifyTime" : "2014-08-07T16:06:00.489Z",
            "lastModifiedBy" : "project: WM Segment-1.96",
            "createTime" : "2014-08-07T16:06:00.489Z"
         },
         {
            "owner" : "project: WM Segment-2.2",
            "propertyId" : "2a2e3466-4b77-11e4-b7f0-00505625f614",
            "propertyName" : "2420",
            "propertySheetId" : "2a2e3467-4b77-11e4-b7f0-00505625f614",
            "modifyTime" : "2014-10-04T03:33:10.819Z",
            "lastModifiedBy" : "project: WM Segment-2.2",
            "createTime" : "2014-10-04T03:33:10.819Z"
         }
      ],
      "createTime" : "2014-07-17T18:49:02.288Z"
   }
}"""

}
