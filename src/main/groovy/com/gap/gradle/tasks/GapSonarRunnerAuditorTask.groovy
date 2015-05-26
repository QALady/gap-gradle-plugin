package com.gap.gradle.tasks

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask
import groovy.xml.MarkupBuilder
import org.gradle.api.Project

class GapSonarRunnerAuditorTask extends WatchmenTask {

    private CommanderClient commanderClient

    final static String AUDITOR_PROPERTY_SHEET = "/projects/WM Segment Registry/ApplySonarRunner/"
    final static String REPORT_FILE_NAME = "gap-sonar-runner-usage.html"


    GapSonarRunnerAuditorTask(Project project, commanderClient = new CommanderClient()) {
        super(project)
        this.commanderClient = commanderClient
    }

    def execute() {
        def sonarPropertySheetList = readPropertySheet()
        List<String> sonarProjects = writeSonarProjectList(sonarPropertySheetList)
        List<String> allProjects = getAllProjects()
        def noSonarProjects = getNoSonarProjects(sonarProjects, allProjects)
        def htmlData = buildHtmlData(sonarProjects, noSonarProjects)
        createOrUpdateContents(htmlData.toString())
    }

    List<String> getAllProjects() {
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

    static def buildHtmlData(def projectsWithSonar, def noSonarProjects) {
        def htmlWriter = new StringWriter()
        def builder = new MarkupBuilder(htmlWriter)
        builder
        builder.html {
            head {
                link( rel:'stylesheet', type:'text/css', href:'https://cdn.datatables.net/1.10.7/css/jquery.dataTables.css')
                script( '', type:'text/javascript', src:'https://code.jquery.com/jquery-1.10.2.min.js')
                script( '', type:'text/javascript', src:'https://cdn.datatables.net/1.10.7/js/jquery.dataTables.js' )
                script """
                    \$(document).ready(function() {
                        \$('#table1').DataTable();
                        \$('#table2').DataTable();
                    } );"""
                title "Auditor For GapSonarRunner"
            }
            body {
                center{
                    h1 "Auditor For GapSonarRunner"
                }

                table(id:'table1', class:'display') {
                    thead {
                        tr {
                            th("Sonar Project:Segment")
                            th("Last Run")
                        }
                    }
                    tfoot {
                        tr {
                            th("Sonar Project:Segment")
                            th("Last Run")
                        }
                    }
                    tbody{
                        projectsWithSonar.each { row ->
                            tr {

                                td(row.get("projectSegment"))
                                td(row.get("lastRun"))
                            }
                        }
                    }
                }
                hr{}
                br{}
                hr{}
                table(id:'table2', class:'display') {
                    thead{
                        tr {
                            th("No Sonar Projects")
                        }
                    }
                    tfoot{
                        tr {
                            th("No Sonar Projects")
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
        return htmlWriter
    }

    static def createOrUpdateContents(String htmlData) {
        def htmlReportFile = new File(REPORT_FILE_NAME)
        htmlReportFile.createNewFile()
        htmlReportFile.write(htmlData)
        println "Created file : ${htmlReportFile.getAbsoluteFile()}"
    }

    static def getNoSonarProjects(List<String> sonarProjects, List<String> allProjects){

        def sonarProjectNamesList = []
        sonarProjects.each{ p ->
            sonarProjectNamesList.push(p.split(':')[0])
        }

        def noSonarProjects = [];
        allProjects.each{ p ->
            if(!sonarProjectNamesList.contains(p)){
                noSonarProjects.push(p)
            }
        }

        return noSonarProjects

    }


}
