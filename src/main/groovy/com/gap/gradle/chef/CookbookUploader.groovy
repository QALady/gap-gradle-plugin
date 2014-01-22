package com.gap.gradle.chef

import static java.lang.System.currentTimeMillis

import org.apache.commons.logging.LogFactory

class CookbookUploader {

    def jenkinsClient
    def pollIntervalMillis
    def timeoutMillis
    def log = LogFactory.getLog(CookbookUploader)

    CookbookUploader(jenkinsClient, pollIntervalMillis = 15000, timeoutMillis = 600000) {
        this.jenkinsClient = jenkinsClient
        this.pollIntervalMillis = pollIntervalMillis
        this.timeoutMillis = timeoutMillis
    }

    def upload(cookbookName, environment) {
        def jobName = "cookbook-${cookbookName}-${environment}".toString()
        def buildNumber = jenkinsClient.startJob(jobName)
        log.info("Beginning upload of cookbook '${cookbookName}' to environment '${environment}' <${getJobUrl(jobName, buildNumber)}>")
        waitForBuildToComplete(jobName, buildNumber)
        if (jenkinsClient.isSuccessful(jobName, buildNumber)) {
            log.info("Successfully uploaded cookbook <${getJobUrl(jobName, buildNumber)}>")
        } else {
            def message = "Jenkins job failed <${getJobUrl(jobName, buildNumber)}>: Console log: ${jenkinsClient.getConsole(jobName, buildNumber)}"
            log.error(message)
            throw new ChefException(message)
        }
    }

    def waitForBuildToComplete(jobName, buildNumber) {
        def start = currentTimeMillis()
        def end = start + timeoutMillis
        while (!jenkinsClient.isFinished(jobName, buildNumber)) {
            log.info("Jenkins job not yet complete, retrying in ${pollIntervalMillis} ms <${getJobUrl(jobName, buildNumber)}>")
            sleep(pollIntervalMillis);
            if (currentTimeMillis() > end) {
                def message = "Timed out after ${timeoutMillis} ms waiting for job to finish <${getJobUrl(jobName, buildNumber)}>"
                log.error(message)
                throw new ChefException(message)
            }
        }
    }

    def getJobUrl(jobName, buildNumber) {
        jenkinsClient.getJobUrl(jobName, buildNumber)
    }
}
