package com.gap.gradle.plugins.xcode

import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Ignore
import org.junit.Test

import static helpers.Assert.taskShouldExist

class GapXcodePluginTest {

    private Project project
    private GapXcodePlugin plugin

//    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()

        Instantiator instantiator = ((ProjectInternal) project).getServices().get(Instantiator)
        plugin = new GapXcodePlugin(instantiator)

        plugin.apply(project)
    }

    @Ignore // FIXME
    @Test
    public void shouldConfigureExistingTasks() throws Exception {
        plugin.apply(project)

        taskShouldExist('airwatchConfigZip', project)
        taskShouldExist('transformJUnitXmlReportToHTML', project)
        taskShouldExist('gcovCoverage', project)
    }

    @Ignore
    @Test
    public void shouldAddNewTasks() throws Exception {

    }

    @Ignore
    @Test
    public void shouldConfigureTaskGraph() throws Exception {

    }

    @Ignore
    @Test
    public void shouldCreateDefaultSigningIdentities() throws Exception {

    }

    @Ignore
    @Test
    public void shouldAllowNewSigningIdentities() throws Exception {

    }

    @Ignore
    @Test
    public void shouldAllowEditSigningIdentities() throws Exception {

    }

    @Ignore
    @Test
    public void shouldDefineProjectVersion() throws Exception {

    }
}
