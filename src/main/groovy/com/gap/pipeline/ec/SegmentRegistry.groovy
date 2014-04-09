package com.gap.pipeline.ec

class SegmentRegistry {

    def commander

    SegmentRegistry(commander = new CommanderClient()){
        this.commander = commander
    }

    void populate(ivyInfo) {
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

    private void setSegmentRegistryValue(ecProjectName, ecProcedureName, key, value){
        commander.setECProperty("/projects[WM Segment Registry]/SegmentRegistry/${ecProjectName}:${ecProcedureName}/${key}", value)
    }

}
