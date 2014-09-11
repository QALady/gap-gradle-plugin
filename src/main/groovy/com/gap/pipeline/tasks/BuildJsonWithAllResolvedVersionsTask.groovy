package com.gap.pipeline.tasks

import com.gap.gradle.ivy.IvyInfo
import com.gap.pipeline.utils.IvyCoordinateParser
import groovy.json.JsonBuilder

/**
 * Created by gopichand on 9/10/14.
 */
class BuildJsonWithAllResolvedVersionsTask extends WatchmenTask {

    private project

    BuildJsonWithAllResolvedVersionsTask(project) {
        super(project)
        this.project = project
    }

    def execute(){
        buildJson()
    }

    def buildJson() {

        def versionsMap = [:]
        def appVersionsMap = [:]
        def cookbookVersionsMap = [:]

        def ivyInfo = new IvyInfo(project)
        def ivyCoordinateParser = new IvyCoordinateParser()
        def ivyCoordinates = ""

        def jsonBuilder = new JsonBuilder()

        ivyInfo.getDependenciesFromISO().each {dep ->
            ivyCoordinates = ivyCoordinateParser.parse(dep)

            if (ivyCoordinates.group =~ /.infra$/){
                cookbookVersionsMap.put(ivyCoordinateParser.getCookbookName(ivyCoordinates.group),ivyCoordinateParser.getCookbookVersion(ivyCoordinates.version))
            } else {
                appVersionsMap.put(ivyCoordinateParser.getAppName(ivyCoordinates.group),ivyCoordinates.version)
            }
        }

        versionsMap.put("apps", appVersionsMap)
        versionsMap.put("cookbooks", cookbookVersionsMap)

        jsonBuilder(versionsMap)
        print jsonBuilder.toString()

    }

}
