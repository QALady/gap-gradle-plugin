package com.gap.gradle.plugins
import com.gap.gradle.airwatch.AirWatchClient
import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.utils.EnvironmentStub
import com.google.common.collect.Sets
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.internal.artifacts.configurations.ConfigurationContainerInternal
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal
import org.gradle.api.internal.project.DefaultProject
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.mockito.Mockito

import static helpers.Assert.taskShouldDependOn
import static helpers.Assert.taskShouldExist
import static helpers.ResolvedArtifactFactory.resolvedArtifact
import static org.hamcrest.CoreMatchers.instanceOf
import static org.junit.Assert.*
import static org.mockito.Matchers.anyObject
import static org.mockito.Matchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class AirWatchPluginTest {

    private Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'airwatch'

        project.ext.set('target', 'target')
        project.ext.set('artifactVersion', 'artifactVersion')
        project.ext.set('awEnv', 'Test')
        project.ext.set('awTestHost', 'awHost')
        project.ext.set('awTestCredentialName', 'awCredentialName')
        project.ext.set('awTestTenantCode', 'awTenantCode')
        project.ext.set('awTestLocationGroupID', "123")
    }

    @Test
    public void shouldAddNewTasks() {
        taskShouldExist('validateProperties', project)

        taskShouldExist('getCredentials', project)
        taskShouldDependOn('getCredentials', 'validateProperties', project)

        taskShouldExist('configureAirWatchEnvironment', project)
        taskShouldDependOn('configureAirWatchEnvironment', 'getCredentials', project)

        taskShouldExist('pushArtifactToAirWatch', project)
        taskShouldDependOn('pushArtifactToAirWatch', 'configureAirWatchEnvironment', project)

        taskShouldExist('installAirwatchGem', project)
        taskShouldExist('extractAirwatchConfig', project)

        taskShouldExist('configureApp', project)
        ["installAirwatchGem", "extractAirwatchConfig", "pushArtifactToAirWatch"].each {
            taskShouldDependOn('configureApp', it, project)
        }
    }

    @Test(expected = Exception)
    public void shouldValidateProperties() {
        project.ext.set('target', null)
        project.ext.set('awEnv', null)
        project.ext.set('awTestHost', null)
        project.ext.set('awTestCredentialName', null)
        project.ext.set('awTestTenantCode', null)
        project.ext.set('awTestLocationGroupID', null)

        def task = project.tasks.findByName('validateProperties')

        task.execute()
    }

    @Test
    public void shouldCreateClient() {
        def getCredentialsTask = project.tasks.findByName('getCredentials')
        getCredentialsTask.ext.userName = { 'myUser' }
        getCredentialsTask.ext.password = { 'myPass' }

        assertFalse(project.hasProperty('awClient'))

        def task = project.tasks.findByName('configureAirWatchEnvironment')
        task.execute()

        assertThat(project.get('awClient'), instanceOf(AirWatchClient.class));
    }

    @Test
    public void shouldThrowExceptionIfIpaNotFoundInArtifacts() {
        project.configurations.create("archives")

        def pushArtifactsTask = project.tasks.pushArtifactToAirWatch
        pushArtifactsTask.dependsOn.clear()

        try {
            pushArtifactsTask.execute()
            fail("Should have thrown exception because there is no IPA to upload")
        } catch (GradleException e) {
            assertEquals("Could not find artifact that matches configured artifact in archives configuration.", e.cause.message)
        }
    }

    @Test
    public void shouldExposePublishedAppIdAsTaskProperty() throws Exception {
        def mockConfigurations = mock(ConfigurationContainerInternal)
        def archivesConfiguration = mock(ConfigurationInternal)
        def resolvedConfiguration = mock(ResolvedConfiguration)
        when(archivesConfiguration.resolvedConfiguration).thenReturn(resolvedConfiguration)
        when(resolvedConfiguration.resolvedArtifacts).thenReturn(Sets.newHashSet(resolvedArtifact(name: 'target')))
        when(mockConfigurations.getAt("archives")).thenReturn(archivesConfiguration)
        ((DefaultProject)project).setConfigurationContainer(mockConfigurations)

        project.awClient = mock(AirWatchClient)
        when(project.awClient.uploadApp(Mockito.any(File), eq('Foobar'), eq('Ipsum lorem'), anyObject())).thenReturn(["Id": ["Value": "456"]])

        project.airwatchUpload {
            appName = 'Foobar'
            appDescription = 'Ipsum lorem'
            artifact.name = 'target'
        }

        def pushArtifactsTask = project.tasks.pushArtifactToAirWatch
        pushArtifactsTask.dependsOn.clear()

        pushArtifactsTask.execute()

        assertEquals("456", pushArtifactsTask.publishedAppId)
    }

    @Test
    public void shouldNotConfigureAppIfAirwatchUploadConfigFileDoesNotExist() throws Exception {
        def configureAppTask = project.tasks.configureApp
        configureAppTask.dependsOn.clear()

        def nonExistantFile = mock(File)
        when(nonExistantFile.exists()).thenReturn(false)
        project.airwatchUpload.configFile = nonExistantFile

        configureAppTask.execute()

        assertEquals(true, configureAppTask.state.skipped)
    }

    @Test
    @Ignore // TODO undo...
    public void shouldInvokeECToolToRetrieveCredentials() {
        def mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)
        when(mockShellCommand.execute(['ectool', 'getFullCredential', '/projects/WM Credentials/credentials/myCredential', '--value', 'userName'])).thenReturn('myUser')
        when(mockShellCommand.execute(['ectool', 'getFullCredential', '/projects/WM Credentials/credentials/myCredential', '--value', 'password'])).thenReturn('myPass')

        def environmentStub = new EnvironmentStub()
        environmentStub.setValue('COMMANDER_JOBID', '1')

        def commanderClient = new CommanderClient(mockShellCommand, environmentStub)

        def awPlugin = new AirWatchPlugin(mock(Instantiator))
        awPlugin.setCommanderClient(commanderClient)

        def dummyProject = ProjectBuilder.builder().build()
        dummyProject.ext.set('awEnv', 'Test')
        dummyProject.ext.set('awTestCredentialName', 'myCredential')

        awPlugin.apply(dummyProject)

        def getCredentialsTask = dummyProject.tasks.findByName('getCredentials')
        getCredentialsTask.execute()

        assertEquals('myUser', getCredentialsTask.userName())
        assertEquals('myPass', getCredentialsTask.password())
    }
}
