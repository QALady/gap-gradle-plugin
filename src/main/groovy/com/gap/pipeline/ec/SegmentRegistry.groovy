package com.gap.pipeline.ec

import com.gap.gradle.ivy.IvyInfo
import com.gap.gradle.utils.ShellCommand




class SegmentRegistry {

    CommanderClient commander

    SegmentRegistry(){
        this.commander = new CommanderClient()
    }

    SegmentRegistry(CommanderClient commander){
        this.commander = commander
    }

    void populate(IvyInfo ivyInfo) {
        def ecProjectName = commander.getECProperty('/myJob/projectName')
        def ecProcedureName = commander.getECProperty('/myJob/liveProcedure')
        def segmentConfig = commander.getSegmentConfig()

        setSegmentRegistryValue(ecProjectName, ecProcedureName, 'ivyIdentifiers', ivyInfo.identifiers().join('\n'))
        setSegmentRegistryValue(ecProjectName, ecProcedureName, 'ivyDependencies', ivyInfo.dependencies().join('\n'))
        setSegmentRegistryValue(ecProjectName, ecProcedureName, 'svnUrl', segmentConfig.scmUrl)
        setSegmentRegistryValue(ecProjectName, ecProcedureName, 'workingDir', segmentConfig.workingDir)
        setSegmentRegistryValue(ecProjectName, ecProcedureName, 'ciDir', segmentConfig.ciDir)
        setSegmentRegistryValue(ecProjectName, ecProcedureName, 'gradleFile', segmentConfig.gradleFile)
    }

    private void setSegmentRegistryValue(def ecProjectName, def ecProcedureName, def key, def value){
        commander.setECProperty("/Projects[WM Segment Registry]/SegmentRegistry/${ecProjectName}:${ecProcedureName}/${key}", value)
    }
}
