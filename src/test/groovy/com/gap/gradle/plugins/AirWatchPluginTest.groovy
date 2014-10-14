package com.gap.gradle.plugins
import com.gap.gradle.airwatch.*
import com.google.common.collect.Sets
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedConfiguration
import org.gradle.api.internal.artifacts.configurations.ConfigurationContainerInternal
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal
import org.gradle.api.internal.project.DefaultProject
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

import static helpers.Assert.taskShouldDependOn
import static helpers.Assert.taskShouldExist
import static helpers.ResolvedArtifactFactory.resolvedArtifact
import static org.junit.Assert.*
import static org.mockito.Matchers.any
import static org.mockito.Mockito.*

class AirWatchPluginTest {

    private Project project
    private AirWatchClientFactory airWatchClientFactory
    private CredentialProvider credentialProvider

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
        airWatchClientFactory = mock(AirWatchClientFactory)
        credentialProvider = mock(CredentialProvider)

        def beginInstallConfigValidator = mock(BeginInstallConfigValidator)
        def mockConfigurations = mock(ConfigurationContainerInternal)
        def archivesConfiguration = mock(ConfigurationInternal)
        def resolvedConfiguration = mock(ResolvedConfiguration)
        when(archivesConfiguration.resolvedConfiguration).thenReturn(resolvedConfiguration)
        when(resolvedConfiguration.resolvedArtifacts).thenReturn(Sets.newHashSet(resolvedArtifact(name: 'target')))
        when(mockConfigurations.getAt("archives")).thenReturn(archivesConfiguration)
        ((DefaultProject)project).setConfigurationContainer(mockConfigurations)

        def airWatchPlugin = new AirWatchPlugin(((ProjectInternal)project).getServices().get(Instantiator))
        airWatchPlugin.airWatchClientFactory = airWatchClientFactory
        airWatchPlugin.credentialProvider = credentialProvider
        airWatchPlugin.beginInstallConfigValidator = beginInstallConfigValidator
        airWatchPlugin.apply(project)
    }

    @Test
    public void shouldAddNewTasks() {
        taskShouldExist('pushArtifactToAirWatch', project)
        taskShouldExist('installAirwatchGem', project)
        taskShouldExist('extractAirwatchConfig', project)

        taskShouldExist('configureApp', project)
        ["installAirwatchGem", "extractAirwatchConfig", "pushArtifactToAirWatch"].each {
            taskShouldDependOn('configureApp', it, project)
        }
    }

    @Test
    public void shouldThrowExceptionIfIpaNotFoundInArtifacts() {
        def pushArtifactsTask = project.tasks.pushArtifactToAirWatch

        project.airwatchUpload {
            artifact.name = 'notFound'
        }

        try {
            pushArtifactsTask.execute()
            fail("Should have thrown exception because there is no IPA to upload")
        } catch (GradleException e) {
            assertEquals("Could not find artifact that matches configured artifact in archives configuration.", e.cause.message)
        }
    }

    @Test
    public void shouldExposePublishedAppIdAsTaskProperty() throws Exception {
        def airWatchClient = mock(AirWatchClient)
        when(airWatchClientFactory.create(project.airwatchUpload.environments.preProduction, credentialProvider)).thenReturn(airWatchClient)
        when(airWatchClient.uploadApp(Mockito.any(File), any(BeginInstallConfig))).thenReturn(["Id": ["Value": "456"]])

        project.airwatchUpload {
            artifact.name = 'target'
            targetEnvironment environments.preProduction
        }

        def pushArtifactsTask = project.tasks.pushArtifactToAirWatch

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
    public void shouldSupportAddingEnvironments() throws Exception {
        project.airwatchUpload.environments {
            example {
                apiHost "http://api.example.com"
                consoleHost "http://console.example.com"
                tenantCode "1234DEF"
                locationGroupId "123"
                credentialName "foobar"
            }
        }

        def exampleEnv = project.airwatchUpload.environments.example
        assertEquals("example", exampleEnv.name)
        assertEquals("http://api.example.com", exampleEnv.apiHost)
        assertEquals("http://console.example.com", exampleEnv.consoleHost)
        assertEquals("1234DEF", exampleEnv.tenantCode)
        assertEquals("123", exampleEnv.locationGroupId)
        assertEquals("foobar", exampleEnv.credentialName)
    }

    @Test
    public void shouldSupportOverwritingValuesOnEnvironments() throws Exception {
        project.airwatchUpload.environments {
            example {
                apiHost "http://api.example.com"
                consoleHost "http://console.example.com"
                tenantCode "1234DEF"
                locationGroupId "123"
                credentialName "foobar"
            }
        }
        project.airwatchUpload.environments {
            example {
                locationGroupId "456"
            }
        }

        def exampleEnv = project.airwatchUpload.environments.example
        assertEquals("456", exampleEnv.locationGroupId)
        assertEquals("example", exampleEnv.name)
        assertEquals("http://api.example.com", exampleEnv.apiHost)
        assertEquals("http://console.example.com", exampleEnv.consoleHost)
        assertEquals("1234DEF", exampleEnv.tenantCode)
        assertEquals("foobar", exampleEnv.credentialName)
    }

    @Test
    public void shouldAddDefaultEnvironments() throws Exception {
        assertNotNull("should have added prod environment", project.airwatchUpload.environments.production)
        assertNotNull("should have added preProd environment", project.airwatchUpload.environments.preProduction)
    }

    @Test
    public void shouldSupportSpecifyingTheTargetEnvironmentToUpload() throws Exception {
        project.airwatchUpload {
            artifact.name = 'target'
            targetEnvironment environments.preProduction
        }

        def airWatchClient = mock(AirWatchClient)
        when(airWatchClientFactory.create(project.airwatchUpload.environments.preProduction, credentialProvider)).thenReturn(airWatchClient)
        when(airWatchClient.uploadApp(Mockito.any(File), any(BeginInstallConfig))).thenReturn(["Id": ["Value": "456"]])

        project.tasks.pushArtifactToAirWatch.execute()

        verify(airWatchClientFactory).create(project.airwatchUpload.environments.preProduction, credentialProvider)
    }
}