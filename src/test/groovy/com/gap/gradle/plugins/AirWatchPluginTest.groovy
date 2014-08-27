package com.gap.gradle.plugins

import static helpers.Assert.taskShouldExist
import static helpers.Assert.taskShouldDependOn
import static org.hamcrest.CoreMatchers.instanceOf
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat

import com.gap.gradle.airwatch.AirWatchClient
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

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
    project.ext.set('awTestUser', 'awUser')
    project.ext.set('awTestPass', 'awPass')
    project.ext.set('awTenantCode', 'awTenantCode')
  }

  @Test
  public void shouldAddNewTasks() {
    taskShouldExist('validateProperties', project)

    taskShouldExist('configureAirWatchEnvironment', project)
    taskShouldDependOn('configureAirWatchEnvironment', 'validateProperties', project)

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
    project.ext.set('awTestUser', null)
    project.ext.set('awTestPass', null)
    project.ext.set('awTenantCode', null)

    def task = project.tasks.findByName('validateProperties')

    task.execute()
  }

  @Test
  public void shouldCreateClient() {
    assertFalse(project.hasProperty('awClient'))

    def task = project.tasks.findByName('configureAirWatchEnvironment')
    task.execute()

    assertThat(project.get('awClient'), instanceOf(AirWatchClient.class));
  }
}
