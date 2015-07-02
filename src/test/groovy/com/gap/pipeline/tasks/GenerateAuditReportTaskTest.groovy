package com.gap.pipeline.tasks

import com.gap.pipeline.ec.CommanderArtifacts
import com.gap.pipeline.ec.CommanderClient
import groovy.mock.interceptor.MockFor
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import static org.junit.rules.ExpectedException.none


class GenerateAuditReportTaskTest {

    def generateAuditReportTask
    def project
    def mockCommanderClient
    def mockCommanderArtifacts
    @Rule
    public final ExpectedException expectedException = none()

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gappipeline'

        project.metaClass.tagMessageComment = 'Deploying to Prod'
        project.metaClass.ticketId = 'T123456'
        project.metaClass.artifactCoordinates = 'com.gap.sandbox:prod_1234'
        project.metaClass.userId = 'testUser'
        project.metaClass.userName = 'testName'
        project.metaClass.startTime = '20140214'

        generateAuditReportTask = new GenerateAuditReportTask(project)
        mockCommanderClient = new MockFor(CommanderClient)
        mockCommanderArtifacts = new MockFor(CommanderArtifacts)
    }

    @Test
    void shouldGenerateChangeListReport_whenValuesAreValid(){
        new File("${project.buildDir}/reports").mkdirs()
        mockCommanderArtifacts.demand.copyToArtifactsDir { }
        mockCommanderArtifacts.demand.publishLinks { }

        mockCommanderClient.demand.getUserId {project.userId}
        mockCommanderClient.demand.getUserName{project.userName}
        mockCommanderClient.demand.getStartTime{project.startTime}
        mockCommanderClient.demand.getUserId {project.userId}
        mockCommanderClient.demand.getUserName{project.userName}
        mockCommanderClient.demand.getStartTime{project.startTime}
        executeTask()
    }

    @Test
    void shouldFailToGenerateChangeListReport_whenValuesAreNotValid(){
        expectedException.expect(Exception)
        new File("${project.buildDir}/reports").mkdirs()

        mockCommanderClient.demand.getUserId { null }
        mockCommanderClient.demand.getUserName { null }
        mockCommanderClient.demand.getStartTime { null }
        executeTask()
    }

    private void setupCommanderClientMocks() {
        mockCommanderClient.demand.getUserId {project.userId}
        mockCommanderClient.demand.getUserName{project.userName}
        mockCommanderClient.demand.getStartTime{project.startTime}
    }
    private void setupDefaultMocks() {
        mockCommanderArtifacts.demand.copyToArtifactsDir { }
        mockCommanderArtifacts.demand.publishLinks { }
        setupCommanderClientMocks()
    }

    private void executeTask() {
        mockCommanderArtifacts.use {
            mockCommanderClient.use {
                generateAuditReportTask.execute()
            }
        }
    }
}
