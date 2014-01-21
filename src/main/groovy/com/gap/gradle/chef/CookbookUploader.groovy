package com.gap.gradle.chef

import static java.lang.System.currentTimeMillis

class CookbookUploader {

    def jenkinsClient
    def pollIntervalMillis
    def timeoutMillis

    CookbookUploader(jenkinsClient, pollIntervalMillis = 5000, timeoutMillis = 600000) {
        this.jenkinsClient = jenkinsClient
        this.pollIntervalMillis = pollIntervalMillis
        this.timeoutMillis = timeoutMillis
    }

    def upload(cookbookName, environment) {
        def jobName = "cookbook-${cookbookName}-${environment}".toString()
        def buildNumber = jenkinsClient.startJob(jobName)
        def start = currentTimeMillis()
        def end = start + timeoutMillis
        while (!jenkinsClient.isFinished(jobName, buildNumber)) {
            sleep(pollIntervalMillis);
            if (currentTimeMillis() > end) {
                throw new ChefException("Timed out waiting for job to finish ${jobName} #${buildNumber}"
                    + " <${jenkinsClient.getJobUrl(jobName, buildNumber)}>")
            }
        }
        if (!jenkinsClient.isSuccessful(jobName, buildNumber)) {
            throw new ChefException("Jenkins cookbook job ${jobName} #${buildNumber} failed"
                + " <${jenkinsClient.getJobUrl(jobName, buildNumber)}>:"
                + " Console log: ${jenkinsClient.getConsole(jobName, buildNumber)}")
        }
    }
}
