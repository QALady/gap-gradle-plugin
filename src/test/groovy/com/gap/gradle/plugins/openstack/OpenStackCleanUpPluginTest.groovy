package com.gap.gradle.plugins.openstack

import com.gap.gradle.plugins.cookbook.ConfigFileResource
import com.gap.gradle.plugins.openstack.OpenStackCleanUpPlugin
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import static helpers.Assert.shouldExecuteTask
import static org.hamcrest.Matchers.*
import static org.junit.Assert.assertThat

class OpenStackCleanUpPluginTest {

    @Rule
    public final ConfigFileResource config = new ConfigFileResource(OpenStackCleanUpPlugin, "CONFIG_FILE")

    private Project project

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'openstack-cleanup'
    }


    @Test
    void shouldUseDefaultConfig_whenConfigFileDoesNotExist() {
        new File(OpenStackCleanUpPlugin.CONFIG_FILE).delete()
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'openstack-cleanup'
        assertThat(project.jenkins.knifeCleanUpJobName, nullValue())

    }

    @Test
    void shouldReadCredentialsFromConfigFile() {
        new File(OpenStackCleanUpPlugin.CONFIG_FILE).write(
            "jenkins.knifeCleanUpJobName=dummyJobName\nchef.environment=tdev"
        )
        def project = ProjectBuilder.builder().build()
        project.apply plugin: 'openstack-cleanup'
        assertThat(project.jenkins.knifeCleanUpJobName, equalTo("dummyJobName"))
    }

    @Test
    void shouldAddCleanUpOrphanObjectsTaskToProject() {
        taskShouldExist('cleanUpOrphanObjects')
    }

    @Test
    void shouldExecuteCleanUpOrphanObjectsTask() {
        shouldExecuteTask(project, 'cleanUpOrphanObjects', CleanUpOrphanObjectsTask)
    }


    def taskShouldExist(task) {
        assertThat(project.tasks.findByName(task), notNullValue())
    }

}
