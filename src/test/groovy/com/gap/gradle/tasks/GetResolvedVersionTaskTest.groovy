package com.gap.gradle.tasks

import static org.mockito.Matchers.anyString
import static org.mockito.Matchers.*
import static org.mockito.Mockito.*

import com.gap.gradle.ivy.IvyInfo
import com.gap.pipeline.ec.CommanderClient
import groovy.mock.interceptor.MockFor
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GetResolvedVersionTaskTest {

    private CommanderClient commanderClient
    private Project project
    private MockFor ivyInfo

    @Before
    public void setUp() {
        project = ProjectBuilder.builder().build()
        project.metaClass.dependencyGroup = 'com.gap.ref-app.infra'
        project.metaClass.dependencyName = 'ci'

        ivyInfo = new MockFor(IvyInfo)
        ivyInfo.demand.getAllResolvedDependencies {
            ["multi-module-app.app-a:webapp-a:1720", "com.gap.ref-app.infra:ci:0.0.46.2407750"] as Set
        }
        commanderClient = mock(CommanderClient.class)
    }


    @Test
    public void execute_shouldSetECPropertyIfParameterIsGiven(){
        project.metaClass.ecProperty = 'refAppCookbookVersion'
        def task = new GetResolvedVersionTask(project, commanderClient)

        ivyInfo.use {
            task.execute()
        }

        verify(commanderClient).setECProperty(eq(GString.EMPTY + "/myJob/refAppCookbookVersion"), eq('0.0.46.2407750'))
    }

    @Test
    public void execute_shouldNotSetECPropertyIfParameterIsNotGiven(){
        def task = new GetResolvedVersionTask(project, commanderClient)
        ivyInfo.use {
            task.execute()
        }

        verify(commanderClient, never()).setECProperty(any(GString), any(GString))
    }

}
