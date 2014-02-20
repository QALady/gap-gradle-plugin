package com.gap.pipeline.tasks

import com.gap.pipeline.ec.CommanderArtifacts
import com.gap.pipeline.ec.CommanderClient
import groovy.mock.interceptor.MockFor
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test


class GenerateAuditReportTaskTest {

    def generateAuditReportTask
    def project
    def mockCommanderClient
    def mockCommanderArtifacts

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gappipeline'

        project.tagMessageComment = 'Deploying to Prod'
        project.ticketId = 'T123456'
        project.artifactCoordinates = 'com.gap.sandbox:prod_1234'

        generateAuditReportTask = new GenerateAuditReportTask(project)
        mockCommanderClient = new MockFor(CommanderClient)
        mockCommanderArtifacts = new MockFor(CommanderArtifacts)
    }

    @Test
    void shouldGenerateAuditReport_whenValuesAreValid(){
        new File("${project.buildDir}/reports").mkdirs()
        setupDefaultMocks()
        executeTask()
    }

    private void setupCommanderClientMocks() {
        mockCommanderClient.demand.getUserId{'ka7q5f6'}
        mockCommanderClient.demand.getUserName{'kamesh'}
        mockCommanderClient.demand.getStartTime{'14022014'}
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
