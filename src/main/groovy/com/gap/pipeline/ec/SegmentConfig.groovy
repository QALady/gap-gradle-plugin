package com.gap.pipeline.ec

  


class SegmentConfig {

    def scmUrl          //corresponds to /myJob/watchmen_config/configSCMUrl
    def workingDir      //corresponds to /myJob/watchmen_config/workingDir
    def ciDir           //corresponds to /myJob/watchmen_config/ciDir
    def gradleFile      //corresponds to /myJob/watchmen_config/gradleFile
    def isManual        //corresponds to /myJob/watchmen_config/isManual
    def projectName     ////corresponds to /myJob/projectName
    def procedureName     ////corresponds to /myJob/procedureName

    SegmentConfig(scmUrl, workingDir, ciDir, gradleFile, isManual, projectName, procedureName) {
        this.scmUrl = scmUrl
        this.workingDir = workingDir
        this.ciDir = ciDir
        this.gradleFile = gradleFile
        this.isManual = isManual
        this.projectName = projectName
        this.procedureName = procedureName
    }
}
