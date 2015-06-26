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

    def sonarProjectsExpectedHtmlDataFile = "src/test/groovy/com/gap/gradle/resources/sonarProjectsExpectedHtmlFileAuditorTaskTest.html"
    def noSonarProjectsExpectedHtmlDataFile = "src/test/groovy/com/gap/gradle/resources/noSonarProjectsExpectedHtmlFileAuditorTaskTest.html"

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
        def sonarProjectsExpectedHtml = new XmlSlurper().parse(new FileReader(sonarProjectsExpectedHtmlDataFile))
        def noSonarProjectsExpectedHtml = new XmlSlurper().parse(new FileReader(noSonarProjectsExpectedHtmlDataFile))

        def sonarProjects = []
        def project1 = [:]
        project1.put("projectSegment", "projectSegment 1")
        project1.put("lastRun", "lastRun 1")
        def project2 = [:]
        project2.put("projectSegment", "projectSegment 2")
        project2.put("lastRun", "lastRun 2")
        sonarProjects.add(project1)
        sonarProjects.add(project2)
        def noSonarProjects = ['NoSonarProject1', 'NoSonarProject2', 'NoSonarProject3']

        def (sonarProjectsHtmlData,noSonarProjectsHtmlData) = auditorTask.buildHtmlData(sonarProjects, noSonarProjects)
        def sonarProjectsActualHtml = new XmlSlurper().parseText(sonarProjectsHtmlData.toString())
        def noSonarProjectsActualHtml = new XmlSlurper().parseText(noSonarProjectsHtmlData.toString())

        Assert.assertEquals(sonarProjectsActualHtml.toString().replaceAll(" ",""), sonarProjectsExpectedHtml.toString().replaceAll(" ",""), "Values mismatch")
        Assert.assertEquals(noSonarProjectsActualHtml.toString().replaceAll(" ",""), noSonarProjectsExpectedHtml.toString().replaceAll(" ",""), "Values mismatch")
    }

    @Test
    void createOrUpdateContents() {
        def expectedHtmlData = new File(sonarProjectsExpectedHtmlDataFile).getText()
        auditorTask.createOrUpdateContents(auditorTask.SONAR_REPORT_FILE_NAME, expectedHtmlData)
        File actualFile = new File(auditorTask.SONAR_REPORT_FILE_NAME)
        Assert.assertEquals(actualFile.getText(), expectedHtmlData, "Values mismatch")
    }

    @Test
    void getNoSonarProjects() {
        def sonarProjects = []
        def temp1=[:]
        temp1.put("projectSegment","project1:holi")
        sonarProjects.add(temp1)
        def temp2=[:]
        temp2.put("projectSegment","project2:chao")
        sonarProjects.add(temp2)
        def temp3=[:]
        temp3.put("projectSegment","project3:teni")
        sonarProjects.add(temp3)
        def allProjects = ["project1","project2","project3","project4", "project-1"]
        def noSonarProjects = auditorTask.getNoSonarProjects(sonarProjects,allProjects)

        Assert.assertEquals(noSonarProjects[0], "project4", "Values mismatch")
        Assert.assertEquals(noSonarProjects[1], "project-1", "Values mismatch")
    }

}
