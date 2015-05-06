package com.gap.gradle.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import groovy.json.JsonSlurper
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito
import org.testng.Assert

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class GapSonarRunnerAuditorTaskTest {

    def logger = LogFactory.getLog(GapSonarRunnerAuditorTaskTest)

    private Project project

    CommanderClient commanderClient

    GapSonarRunnerAuditorTask auditorTask

    ShellCommand mockShellCommand

    def expectedJsonDataFile = "src/test/groovy/com/gap/gradle/resources/expectedJsonFileAuditorTaskTest.json"

    def expectedHtmlDataFile = "src/test/groovy/com/gap/gradle/resources/expectedHtmlFileAuditorTaskTest.html"

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Before
    void setup() {

        project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build();

        project.apply plugin: 'sonar-runner'

        mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)

        commanderClient = new CommanderClient(mockShellCommand)

        auditorTask = new GapSonarRunnerAuditorTask(project, commanderClient)

        when(mockShellCommand.execute(['ectool', '--format', 'json', 'getProperties', '--path', '/projects/WM Segment Registry/ApplySonarRunner/'])).thenReturn(new File(expectedJsonDataFile).getText())
    }

    @Test
    void readPropertySheetTest() {
        def jsonSlurpedExpected = new JsonSlurper().parse(new FileReader(expectedJsonDataFile))

        def jsonSlurpedActual = auditorTask.readPropertySheet()
        Assert.assertEquals(jsonSlurpedActual, jsonSlurpedExpected, "Values does not match")
    }

    @Test
    void buildHtmlData() {
        def expectedHtml = new XmlSlurper().parse(new FileReader(expectedHtmlDataFile))

        def projectList = []
        def project1 = [:]
        project1.put("projectSegment", "projectSegment 1")
        project1.put("lastRun", "lastRun 1")
        def project2 = [:]
        project2.put("projectSegment", "projectSegment 2")
        project2.put("lastRun", "lastRun 2")
        projectList.add(project1)
        projectList.add(project2)

        def actualHtml = new XmlSlurper().parseText(auditorTask.buildHtmlData(projectList).toString())

        Assert.assertEquals(actualHtml, expectedHtml, "Values mismatch")
    }

    @Test
    void createOrUpdateContents() {
        def expectedHtmlData = new File(expectedHtmlDataFile).getText()
        auditorTask.createOrUpdateContents(expectedHtmlData)
        File actualFile = new File(auditorTask.REPORT_FILE_NAME)
        Assert.assertEquals(actualFile.getText(), expectedHtmlData, "Values mismatch")
    }
}
