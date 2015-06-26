package com.gap.gradle.tasks

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask
import groovy.xml.MarkupBuilder
import org.gradle.api.Project

import org.apache.commons.logging.LogFactory

class GapSonarRunnerAuditorTask extends WatchmenTask {

    private static final logger = LogFactory.getLog(GapSonarRunnerAuditorTask)

    private CommanderClient commanderClient

    final static String AUDITOR_PROPERTY_SHEET = "/projects/WM Segment Registry/ApplySonarRunner/"
    final static String SONAR_REPORT_FILE_NAME = "gap-sonar-runner-usage.html"
    final static String NO_SONAR_REPORT_FILE_NAME = "gap-nosonar-runner-usage.html"


    GapSonarRunnerAuditorTask(Project project, commanderClient = new CommanderClient()) {
        super(project)
        this.commanderClient = commanderClient
    }

    def execute() {
        def sonarPropertySheetList = readPropertySheet()
        List sonarProjects = writeSonarProjectList(sonarPropertySheetList)
        List allProjects = getAllProjects()
        def noSonarProjects = getNoSonarProjects(sonarProjects, allProjects)
        def (sonarProjectsHtmlData,noSonarProjectsHtmlData) = buildHtmlData(sonarProjects, noSonarProjects)
        createOrUpdateContents(SONAR_REPORT_FILE_NAME, sonarProjectsHtmlData.toString())
        createOrUpdateContents(NO_SONAR_REPORT_FILE_NAME, noSonarProjectsHtmlData.toString())

    }

    List getAllProjects() {
        def projects= commanderClient.getProjects()
        return projects.project.projectName
    }

    def readPropertySheet() {
        Map pConfig = [:]
        pConfig.put("path", AUDITOR_PROPERTY_SHEET)
        return commanderClient.getECProperties(pConfig)
    }

    static def writeSonarProjectList(def jsonSlurped) {
        def projectList = []
        jsonSlurped.propertySheet.property.each { it ->
            def projectMap = [:]
            projectMap.put("projectSegment", it.propertyName)
            projectMap.put("lastRun", it.value)
            projectList.add(projectMap)
        }

        return projectList
    }

    static def buildHtmlData(def sonarProjects, def noSonarProjects) {
        def htmlWriterSonarProjects = new StringWriter()
        def builderSonarProjects = new MarkupBuilder(htmlWriterSonarProjects)
        builderSonarProjects.html {
            head {
                title "Auditor For GapSonarRunner - Sonar Projects"
            }
            body {
                center{
                    h1 "Auditor For GapSonarRunner - Sonar Projects"
                }

                table(border:'1', cellspacing:'1', cellpadding:'4') {
                    thead {
                        tr {
                            th("Sonar Project:Segment")
                            th("Last Run")
                        }
                    }
                    tbody{
                        sonarProjects.each { row ->
                            tr {

                                td(row.get("projectSegment"))
                                td(row.get("lastRun"))
                            }
                        }
                    }
                }
            }
        }

        def htmlWriterNoSonarProjects = new StringWriter()
        def builderNoSonarProjects = new MarkupBuilder(htmlWriterNoSonarProjects)
        builderNoSonarProjects.html {
            head {
                title "Auditor For GapSonarRunner -  Non Sonar Projects"
            }
            body {
                center{
                    h1 "Auditor For GapSonarRunner -  Non Sonar Projects"
                }

                table(border:'1', cellspacing:'1', cellpadding:'4') {
                    thead{
                        tr {
                            th("Non Sonar Projects")
                        }
                    }
                    tbody{
                        noSonarProjects.each { row ->
                            tr {
                                td(row)
                            }

                        }
                    }
                }
            }
        }

        return [htmlWriterSonarProjects, htmlWriterNoSonarProjects]
    }

    static def createOrUpdateContents(String reportName, String htmlData) {
        def htmlReportFile = new File(reportName)
        htmlReportFile.createNewFile()
        htmlReportFile.write(htmlData)
        logger.info("Created file : ${htmlReportFile.getAbsoluteFile()}")
    }

    static def getNoSonarProjects(List<LinkedHashMap> sonarProjects, List allProjects){

        def sonarProjectNamesList = []
        sonarProjects.each{ p ->
            logger.info("Elements is : ${p}")
            String projectSegment = p.get("projectSegment").toString().split(':')[0]
            sonarProjectNamesList.add(projectSegment)
        }

        def noSonarProjects = [];
        allProjects.each{ p ->
            if(!sonarProjectNamesList.contains(p)){
                noSonarProjects.add(p)
            }
        }

        return noSonarProjects

    }


}
