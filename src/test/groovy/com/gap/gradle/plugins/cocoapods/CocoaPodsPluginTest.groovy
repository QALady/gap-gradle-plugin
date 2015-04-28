package com.gap.gradle.plugins.cocoapods

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static helpers.Assert.*

public class CocoaPodsPluginTest {

    private static final String UPDATE_TASK = "updatePodspec"
    private static final String PUSH_TASK = "pushPodspec"

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

        taskShouldExist(PUSH_TASK, project)
        taskShouldBeOfType(PUSH_TASK, UploadPodspecTask, project)

        taskShouldDependOn(PUSH_TASK, UPDATE_TASK, project)
    }
}
