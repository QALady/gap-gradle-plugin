package com.gap.gradle.tasks

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask
import groovy.xml.MarkupBuilder
import org.gradle.api.Project

class GapSonarRunnerAuditorTask extends WatchmenTask{

    private CommanderClient commanderClient

    final static String AUDITOR_PROPERTY_SHEET = "/projects/WM Segment Registry/ApplySonarRunner/"
    final static String REPORT_FILE_NAME = "gap-sonar-runner-usage.html"


    GapSonarRunnerAuditorTask(Project project, commanderClient= new CommanderClient()) {
        super(project)
        this.commanderClient = commanderClient
    }

    def execute(){
        def jsonSlurped = readPropertySheet()
        def projectList = writeProjectList(jsonSlurped)
        def htmlData = buildHtmlData(projectList)
        createOrUpdateContents(htmlData)
    }

    def readPropertySheet(){
        Map pConfig=[:]
        pConfig.put("path", AUDITOR_PROPERTY_SHEET)
        return commanderClient.getECProperties(pConfig)
    }

    static def writeProjectList(def jsonSlurped){
        def projectList=[]
        jsonSlurped.propertySheet.property.each{ it ->
            def projectMap=[:]
            projectMap.put("projectSegment", it.propertyName)
            projectMap.put("lastRun", it.value)
            projectList.add(projectMap)
        }

        return projectList
    }

    static def buildHtmlData(def projectList){
        def htmlWriter = new StringWriter()
        def builder = new MarkupBuilder(htmlWriter)
        builder.html{
            head{
                title   "Auditor For Gap Sonar Runner"
            }
            body{
                h1  "Auditor For Gap Sonar Runner"

                table{
                    projectList.each { row ->
                        tr{
                            td(row)
                                    {
                                        td(row.get("projectSegment"))
                                        td(row.get("lastRun"))
                                    }
                        }

                    }
                }
            }
        }
        return htmlWriter
    }

    static def createOrUpdateContents(def htmlData){
        def htmlReportFile = new File(REPORT_FILE_NAME)
        htmlReportFile.createNewFile()
        htmlReportFile.write(htmlData)
        println "Created file : ${htmlReportFile.getAbsoluteFile()}"
    }


}
