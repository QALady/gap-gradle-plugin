package com.gap.pipeline.ec




class SegmentConfig {

    def scmUrl          //corresponds to /myJob/watchmen_config/configSCMUrl
    def workingDir      //corresponds to /myJob/watchmen_config/workingDir
    def ciDir           //corresponds to /myJob/watchmen_config/ciDir
    def gradleFile      //corresponds to /myJob/watchmen_config/gradleFile
    def isManual        //corresponds to /myJob/watchmen_config/isManual
    def scmConfigName   //corresponds to /myJob/watchmen_config/scmConfigName

    SegmentConfig(scmUrl, workingDir, ciDir, gradleFile, scmConfigName, isManual) {
        this.scmUrl = scmUrl
        this.workingDir = workingDir
        this.ciDir = ciDir
        this.gradleFile = gradleFile
        this.isManual = isManual
        this.scmConfigName = scmConfigName
    }
}
