package com.gap.gradle.tasks

import com.gap.gradle.ivy.IvyInfo
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import com.gap.pipeline.utils.IvyCoordinateParser

@RequiredParameters([
@Require(parameter = 'dependencyGroup', description = 'group of the dependency we want to resolve'),
@Require(parameter = 'dependencyName', description = 'name of the dependency we want to resolve')
])
class GetResolvedVersionTask extends WatchmenTask {

    private def commanderClient

    GetResolvedVersionTask(project, commanderClient = new CommanderClient()) {
        super(project)
        this.commanderClient = commanderClient
    }

    def execute () {
        def version = findVersion(project.dependencyGroup, project.dependencyName)
        print "resolved version of ${project.dependencyGroup}:${project.dependencyName} is ${version}"
        if (project.hasProperty('ecProperty')) {
            commanderClient.setProperty("/myJob/${project.ecProperty}", version)
            print "property /myJob/${project.ecProperty} set to value ${version}"
        }
    }

    def findVersion(String group, String name) {
        def version
        def ivyInfo = new IvyInfo(project)
        ivyInfo.allResolvedDependencies.each {dep ->
            def coordinates = new IvyCoordinateParser().parse(dep)
            if(coordinates.group.equals(group) && coordinates.name.equals(name)) {
                version = coordinates.version
            }
        }
        return version
    }

}
