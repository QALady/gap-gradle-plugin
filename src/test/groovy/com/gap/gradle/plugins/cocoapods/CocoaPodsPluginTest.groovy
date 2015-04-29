package com.gap.gradle.plugins.cocoapods

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static helpers.Assert.*

public class CocoaPodsPluginTest {

    private static final String UPDATE_TASK = "updatePodspec"
    private static final String PUBLISH_TASK = "publishPod"

    private Project project

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()

        new CocoaPodsPlugin().apply(project)
    }

    @Test
    public void shouldAddNewTasks() throws Exception {
        taskShouldExist(UPDATE_TASK, project)
        taskShouldBeOfType(UPDATE_TASK, UpdatePodspecTask, project)

        taskShouldExist(PUBLISH_TASK, project)
        taskShouldBeOfType(PUBLISH_TASK, PublishPodTask, project)

        taskShouldDependOn(PUBLISH_TASK, UPDATE_TASK, project)
    }
}
