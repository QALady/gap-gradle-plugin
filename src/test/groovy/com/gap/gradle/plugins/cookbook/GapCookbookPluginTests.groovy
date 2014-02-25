package com.gap.gradle.plugins.cookbook

import static helpers.Assert.shouldExecuteTask
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GapCookbookPluginTests {

    @Rule
    public final ConfigFileResource config = new ConfigFileResource(GapCookbookPlugin, "CONFIG_FILE")

    private Project project

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
    }

    @Test
    void publishCookbookToArtifactoryTaskIsAddedToProject(){
        taskShouldExist('publishCookbookToArtifactory')
    }

    @Test
    void publishCookbookToChefServerTaskIsAddedToProject () {
        taskShouldExist('publishCookbookToChefServer')
    }

    @Test
    void shouldExecutePublishCookbookToChefServerTask (){
        shouldExecuteTask(project, 'publishCookbookToChefServer', PublishCookbookToChefServerTask)
    }

    @Test
    void shouldExecutePublishCookbookToArtifactoryTask (){
        shouldExecuteTask(project, 'publishCookbookToArtifactory', PublishCookbookToArtifactoryTask)
    }

    @Test
    void shouldUseDefaultConfig_whenConfigFileDoesNotExist() {
        new File(GapCookbookPlugin.CONFIG_FILE).delete()
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        assertThat(project.jenkins.cookbookServerUrl, nullValue())
        assertThat(project.chef.environment, equalTo('tdev'))
    }

    @Test
    void shouldReadCredentialsFromConfigFile() {
        new File(GapCookbookPlugin.CONFIG_FILE).write(
            "jenkins.cookbookServerUrl=http://my.jenkins.server\n"
            + "chef.environment=prod"
        )
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapcookbook'
        assertThat(project.jenkins.cookbookServerUrl, equalTo("http://my.jenkins.server"))
        assertThat(project.chef.environment, equalTo("prod"))
    }

    @Test
    void shouldAddValidateCookbookDependenciesTaskToProject() {
        taskShouldExist('validateCookbookDependencies')
    }

    @Test
    void shouldExecuteValidateCookbookDependenciesTask() {
        shouldExecuteTask(project, 'validateCookbookDependencies', ValidateCookbookDependenciesTask)
    }

    @Test
    void validateCookbook_shouldDependOnValidateCookbookDependencies() {
        taskShouldDependOn('validateCookbook', 'validateCookbookDependencies')
    }

    @Test
    void validateCookbook_shouldDependOnValidateTransitiveCookbookDependencies() {
        taskShouldDependOn('validateCookbook', 'validateTransitiveCookbookDependencies')
    }

    @Test
    void publishCookbookToArtifactory_shouldDependOnValidateCookbook() {
        taskShouldDependOn('publishCookbookToArtifactory', 'validateCookbook')
    }

    @Test
    void publishCookbookToChefServer_shouldDependOnValidateCookbook() {
        taskShouldDependOn('publishCookbookToChefServer', 'validateCookbook')
    }

    @Test
    void shouldAddGenerateCookbookMetadataTaskToProject() {
        taskShouldExist('generateCookbookMetadata')
    }

    @Test
    void shouldExecuteGenerateCookbookMetadataTask() {

    }

    @Test
    void validateCookbookDependencies_shouldDependOnGenerateCookbookMetadata() {
        taskShouldDependOn('validateCookbookDependencies', 'generateCookbookMetadata')
    }

    @Test
    void publishCookbookToArtifactory_shouldDependOnGenerateCookbookMetadata() {
        taskShouldDependOn('publishCookbookToArtifactory', 'generateCookbookMetadata')
    }

    @Test
    void publishCookbookToChefServer_shouldDependOnGenerateCookbookMetadata() {
        taskShouldDependOn('publishCookbookToChefServer', 'generateCookbookMetadata')
    }

    @Test
    void shouldAddValidateTransitiveCookbookDependenciesTaskToProject() {
        taskShouldExist('validateTransitiveCookbookDependencies')
    }

    @Test
    void validateTransitiveCookbookDependencies_shouldDependOnValidateCookbookDependencies() {
        taskShouldDependOn('validateTransitiveCookbookDependencies', 'validateCookbookDependencies')
    }

    def taskShouldExist(task) {
        assertThat(project.tasks.findByName(task), notNullValue())
    }

    def taskShouldDependOn(task, requiredDependency) {
        for (def dependency : project.tasks.findByName(task).dependsOn) {
            if (dependency == requiredDependency) {
                return
            } else if (dependency instanceof List) {
                for (def d : dependency) {
                    if (d == requiredDependency) {
                        return
                    }
                }
            }
        }
        fail("Task ${task} does not declare a dependency on ${requiredDependency}")
    }
}
