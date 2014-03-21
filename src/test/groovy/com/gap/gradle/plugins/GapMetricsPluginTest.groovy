package com.gap.gradle.plugins
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.sonar.SonarPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.collection.IsArrayContaining
import org.junit.Test

import static org.hamcrest.CoreMatchers.instanceOf
import static org.hamcrest.CoreMatchers.is
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertThat

class GapMetricsPluginTest {

    @Test
    void shouldNotApplySonarPlugin_whenProjectDoesNotHaveJavaPlugin(){
        def project = ProjectBuilder.builder().build()
        new GapMetricsPlugin().apply(project)
        assertThat project.plugins, not(hasItem(instanceOf(SonarPlugin)))
    }

    @Test
    void shouldApplySonarPlugin_whenProjectHasJavaPlugin(){
        def project = ProjectBuilder.builder().build();
        project.apply plugin:JavaPlugin
        new GapMetricsPlugin().apply(project)
        assertThat project.plugins.toArray(), IsArrayContaining.hasItemInArray(instanceOf(SonarPlugin))
        // nicer hamcrest assertions will come with gradle 1.4 (once junit 4.10 is no longer in the plugin classpath)
        //assertThat project.plugins, hasItem(instanceOf(SonarPlugin))
    }

    @Test
    void shouldDefineCoberturaTasks_whenProjectHasJavaPlugin(){
        def project = ProjectBuilder.builder().build();
        project.apply plugin:JavaPlugin
        new GapMetricsPlugin().apply(project)
        def taskNames = (project.tasks.collect {it.name}).toArray()
        assertThat taskNames, IsArrayContaining.hasItemInArray('coberturaReport')
        // nicer hamcrest assertions will come with gradle 1.4 (once junit 4.10 is no longer in the plugin classpath)
        //  assertThat taskNames, hasItem('coberturaReport')
    }

    @Test
    void shouldConfigureSonarWithProjectDefinedProperties(){

        def project = ProjectBuilder.builder().build();
        project.ext.sonarUrl = 'http://expected_sonar_url'
        project.ext.sonarUsername = 'expected_sonar_user'
        project.ext.sonarPassword = 'expected_sonar_password'
        project.ext.sonarDatabaseUrl = 'jdbc:mysql://expected_host'

        project.apply plugin:JavaPlugin
        new GapMetricsPlugin().apply(project)

        assertThat 'server url', project.sonar.server.url, is('http://expected_sonar_url')
        assertThat 'database url', project.sonar.database.url, is('jdbc:mysql://expected_host')
        assertThat 'user name', project.sonar.database.username, is('expected_sonar_user')
        assertThat 'user password', project.sonar.database.password, is('expected_sonar_password')
    }

    @Test
    void shouldConfigureSonarWithDefaults_whenProjectDefinedPropertiesAreNotSet(){

        def project = ProjectBuilder.builder().build();
        // ext api doesn't allow us to remove properties, so we'll just assert that they don't exist
        assertFalse project.ext.has('sonarUrl')
        assertFalse project.ext.has('sonarUsername')
        assertFalse project.ext.has('sonarPassword')
        assertFalse project.ext.has('sonarDatabaseUrl')

        project.apply plugin:JavaPlugin
        new GapMetricsPlugin().apply(project)

        assertThat 'default server url', project.sonar.server.url, is('http://ci.gap.dev/sonar')
        assertThat 'default database url', project.sonar.database.url, is('jdbc:mysql://ci.gap.dev:3306/sonar?')
        assertThat 'default user name', project.sonar.database.username, is('')
        assertThat 'default user password', project.sonar.database.password, is('')
    }

    @Test
    void shouldConfigureSonarForDryRunByDefault(){
        def project = ProjectBuilder.builder().build();

        project.apply plugin:JavaPlugin
        new GapMetricsPlugin().apply(project)

        def properties = new Properties()

        project.sonar.project.propertyProcessors.each {
            it.call properties
        }
        assertThat properties, hasEntry('sonar.dryRun', GString.EMPTY + 'true')
    }

    @Test
    void shouldConfigureSonarDryRunWithProjectDefinedProperty(){
        def project = ProjectBuilder.builder().build();
        project.ext.sonarDryRun = false

        project.apply plugin:JavaPlugin
        new GapMetricsPlugin().apply(project)

        def properties = new Properties()

        project.sonar.project.propertyProcessors.each {
            it.call properties
        }
        assertThat properties, hasEntry('sonar.dryRun', GString.EMPTY + 'false')
    }
}

