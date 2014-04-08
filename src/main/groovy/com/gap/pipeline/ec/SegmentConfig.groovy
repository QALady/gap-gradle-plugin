package com.gap.pipeline.ec

  


class SegmentConfig {

    def scmUrl          //corresponds to /myJob/watchmen_config/configSCMUrl
    def workingDir      //corresponds to /myJob/watchmen_config/workingDir
    def ciDir           //corresponds to /myJob/watchmen_config/ciDir
    def gradleFile      //corresponds to /myJob/watchmen_config/gradleFile

    SegmentConfig(scmUrl, workingDir, ciDir, gradleFile) {
        this.scmUrl = scmUrl
        this.workingDir = workingDir
        this.ciDir = ciDir
        this.gradleFile = gradleFile
    }

    def getScmUrl() {
        return scmUrl
    }

    def getWorkingDir() {
        return workingDir
    }

    def getCiDir() {
        return ciDir
    }

    def getGradleFile() {
        return gradleFile
    }
}
