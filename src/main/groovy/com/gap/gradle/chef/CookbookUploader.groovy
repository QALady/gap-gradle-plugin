package com.gap.gradle.chef

import org.apache.commons.logging.LogFactory

import static java.lang.System.currentTimeMillis

class CookbookUploader {

    def jenkinsClient
    def pollIntervalMillis
    def timeoutMillis
    def log = LogFactory.getLog(CookbookUploader)
    def CookbookUtil cookbookUtil

    CookbookUploader(jenkinsClient, pollIntervalMillis = 15000, timeoutMillis = 1800000, cookbookUtil = new CookbookUtil()) {
        this.cookbookUtil = cookbookUtil
        this.jenkinsClient = jenkinsClient
        this.pollIntervalMillis = pollIntervalMillis
        this.timeoutMillis = timeoutMillis
    }

    def upload(environment, cookbookMetadata) {
        def jobName = "cookbook-${cookbookMetadata.name}-${environment}".toString()
        def buildNumber = jenkinsClient.startJob(jobName)
        log.info("Beginning upload of cookbook '${cookbookMetadata.name}' to environment '${environment}' <${getJobUrl(jobName, buildNumber)}>")
        waitForBuildToComplete(jobName, buildNumber, cookbookMetadata)
        //if cookbook has been uploaded to chef, we consider it a success even if jenkins job is not complete. this is
        //to improve the performance of the cookbook upload
        if (cookbookUtil.doesCookbookExist(cookbookMetadata)  || jenkinsClient.isSuccessful(jobName, buildNumber)) {
            log.info("Successfully uploaded cookbook <${getJobUrl(jobName, buildNumber)}>")
        } else {
            def message = "Jenkins job failed <${getJobUrl(jobName, buildNumber)}>: Console log: ${jenkinsClient.getConsole(jobName, buildNumber)}"
            log.error(message)
            throw new ChefException(message)
        }
    }

    def waitForBuildToComplete(jobName, buildNumber, cookbookMetadata) {
        def start = currentTimeMillis()
        def end = start + timeoutMillis
        while (!jenkinsClient.isFinished(jobName, buildNumber)) {
            log.info("Jenkins job not yet complete, retrying in ${pollIntervalMillis} ms <${getJobUrl(jobName, buildNumber)}>")

            if (cookbookUtil.doesCookbookExist(cookbookMetadata)) {
                log.info("Cookbook ${cookbookMetadata.name} with version ${cookbookMetadata.version} is Uploaded in Chef Server. Exiting from Junkin's job...")
                break;
            } else {
                sleep(pollIntervalMillis);
            }
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
