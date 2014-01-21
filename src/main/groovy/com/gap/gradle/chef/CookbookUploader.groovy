package com.gap.gradle.chef

class CookbookUploader {

    def jenkinsClient

    CookbookUploader(jenkinsClient){
        this.jenkinsClient = jenkinsClient
    }

    def upload(cookbookName, environment) {
        def jobName = "cookbook-${cookbookName}-${environment}".toString()
        def buildNumber = jenkinsClient.startJob(jobName)
        while (!jenkinsClient.isFinished(jobName, buildNumber)) {
            sleep(1000);
        }
        if (!jenkinsClient.isSuccessful(jobName, buildNumber)) {
            throw new ChefException("Jenkins cookbook job ${jobName} #${buildNumber} failed"
                + "[${jenkinsClient.getJobUrl(jobName, buildNumber)}]: Console log: ${jenkinsClient.getConsole(jobName, buildNumber)}")
        }
    }
}
