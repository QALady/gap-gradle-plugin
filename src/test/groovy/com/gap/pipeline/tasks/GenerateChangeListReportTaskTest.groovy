package com.gap.pipeline.tasks

import com.gap.pipeline.ec.CommanderArtifacts
import com.gap.pipeline.ec.CommanderClient
import groovy.mock.interceptor.MockFor
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

class GenerateChangeListReportTaskTest {

    def generateChangeListReportTask
    def project
    def mockCommanderClient
    def mockCommanderArtifacts

	final static String sha1_1 = "6dc4a1a3748a29ec8a8e46fbdcd22b1e55206999,87986d3e66cd088804c5cf1b822aa155e1b03f00"
	final static String cookbookSha1Id_1 = "bae7745b6577b402d946d35587bf629b3814210a"

    @Before
    void setUp (){
        project = ProjectBuilder.builder().build()
        project.apply plugin: 'gappipeline'

        project.prodPrepare.sha1Ids = "${sha1_1}"
		project.prodPrepare.appVersion = 'default'
		project.prodPrepare.cookbookName = "test-ref-app"
		project.prodPrepare.cookbookSha1Id = cookbookSha1Id_1
        project.prodPrepare.roleName = 'webposCompile'
        project.prodPrepare.nodes = 'dgphxposci004.phx.gapinc.dev'
        project.prodPrepare.isRPM = true

        generateChangeListReportTask = new GenerateChangeListReportTask(project)
        mockCommanderClient = new MockFor(CommanderClient)
        mockCommanderArtifacts = new MockFor(CommanderArtifacts)
    }

    @Test
    void shouldGenerateChangeListReport_whenValuesAreValid(){
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
                generateChangeListReportTask.execute()
            }
        }
    }
}
