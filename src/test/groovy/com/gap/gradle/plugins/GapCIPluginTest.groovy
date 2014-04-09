package com.gap.gradle.plugins

import static helpers.Assert.taskShouldExist

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GapCIPluginTest {

    Project project

    @Before
    void setup() {
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gapci'
    }

    @Test
    public void shouldAddMetricsTask(){
        taskShouldExist('metrics', project)
    }

    @Test
    public void shouldAddPrecommitTask(){
        taskShouldExist('precommit', project)
    }

    @Test
    public void shouldAddCommitStageTask(){
        taskShouldExist('commit-stage', project)
    }
}
