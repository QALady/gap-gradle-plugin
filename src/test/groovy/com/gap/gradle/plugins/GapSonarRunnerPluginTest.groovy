package com.gap.gradle.plugins
import static helpers.Assert.shouldExecuteTask
import static helpers.Assert.taskShouldExist
import static junit.framework.Assert.assertTrue
import static org.hamcrest.CoreMatchers.is
import static org.junit.Assert.assertThat

import com.gap.pipeline.tasks.SonarLinkTask
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GapSonarRunnerPluginTest {
    private Project project

    @Before
    public void setUp(){
        project = ProjectBuilder.builder().build()
    }

    @Test
    public void shouldApplySonarRunnerPlugin(){
        project.apply plugin: 'gap-sonar-runner'
        assertThat(project.plugins.hasPlugin('sonar-runner'), is(true))
    }

    @Test
    public void shouldApplyJaCoCoPlugin(){
        project.apply plugin: 'gap-sonar-runner'
        assertThat(project.plugins.hasPlugin('jacoco'), is(true))
    }

    @Test
    public void shouldAddJaCoCoTasks(){
        project.apply plugin: 'java'
        project.apply plugin: 'gap-sonar-runner'
        taskShouldExist('jacocoTestReport', project)
        taskShouldExist('jacocoMerge', project)
        taskShouldExist('jacoco', project)
    }

    @Test
    public void jacoco_shouldDependOnJacocoMerge(){
        project.apply plugin: 'gap-sonar-runner'
        assertTrue(project.tasks.jacoco.dependsOn.contains('jacocoMerge'))
    }

    @Test
    public void jacocoMerge_shouldDependOnTestTasks(){
        def childProject = ProjectBuilder.builder().withName('child1').withParent(project).build()
        childProject.apply plugin: 'java'

        project.apply plugin: 'gap-sonar-runner'
        def childTest = project.tasks.findByPath(":child1:test")


        assertThat(project.tasks.jacocoMerge.taskDependencies.getDependencies().contains(childTest), is(true))
    }

    @Test
    public void sonarRunner_shouldDependOnJacoco(){
        project.apply plugin: 'gap-sonar-runner'
        assertTrue(project.tasks.sonarRunner.dependsOn.contains('jacoco'))
    }

    @Test
    void ivyIdentifiersTaskShouldBeAddedToProject() {
        project.apply plugin: 'gap-sonar-runner'
        taskShouldExist('sonar', project)
    }

    @Test
    void shouldExecuteSonarLinkTask() {
        project.apply plugin: 'gap-sonar-runner'
        shouldExecuteTask(project,'sonar', SonarLinkTask)
    }

    @Test
    void sonar_shouldDependOnSonarRunner(){
        project.apply plugin: 'gap-sonar-runner'
        assertTrue(project.tasks.sonar.dependsOn.contains('sonarRunner'))
    }

}
