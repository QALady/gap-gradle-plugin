package com.gap.gradle.plugins

import static helpers.Assert.taskShouldExist
import static helpers.Assert.taskShouldDependOn
import static org.hamcrest.CoreMatchers.instanceOf
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.*

import com.gap.gradle.airwatch.AirWatchClient
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import com.gap.pipeline.utils.EnvironmentStub
import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import org.mockito.Mockito


import static org.mockito.Mockito.*
import com.gap.pipeline.utils.EnvironmentStub
import com.gap.gradle.utils.ShellCommand
import org.mockito.Mockito
import com.gap.pipeline.ec.CommanderClient

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
  }

  @Test
  public void shouldAddNewTasks() {
    taskShouldExist('validateProperties', project)

    taskShouldExist('getCredentials', project)
    taskShouldDependOn('getCredentials', 'validateProperties', project)

    taskShouldExist('configureAirWatchEnvironment', project)
    taskShouldDependOn('configureAirWatchEnvironment', 'getCredentials', project)

    taskShouldExist('configureArtifactDependency', project)
    taskShouldDependOn('configureArtifactDependency', 'configureAirWatchEnvironment', project)

    taskShouldExist('pushArtifactToAirWatch', project)
    taskShouldDependOn('pushArtifactToAirWatch', 'configureArtifactDependency', project)
  }

  @Test(expected = Exception)
  public void shouldValidateProperties() {
    project.ext.set('target', null)
    project.ext.set('artifactVersion', null)
    project.ext.set('awEnv', null)
    project.ext.set('awTestHost', null)
    project.ext.set('awTestCredentialName', null)
    project.ext.set('awTestTenantCode', null)

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
  public void shouldInvokeECToolToRetrieveCredentials() {
    def mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)
    when(mockShellCommand.execute(['ectool', 'getFullCredential', 'myCredential', '--value', 'userName'])).thenReturn('myUser')
    when(mockShellCommand.execute(['ectool', 'getFullCredential', 'myCredential', '--value', 'password'])).thenReturn('myPass')

    def environmentStub = new EnvironmentStub()
    environmentStub.setValue('COMMANDER_JOBID', '1')

    def commanderClient = new CommanderClient(mockShellCommand, environmentStub)
    def awPlugin = new AirWatchPlugin(commanderClient)

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
