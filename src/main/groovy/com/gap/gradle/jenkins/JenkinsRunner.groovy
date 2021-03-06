package com.gap.gradle.jenkins

import com.gap.pipeline.ec.CommanderClient
import org.apache.commons.logging.LogFactory

class JenkinsRunner {
	JenkinsClient jenkinsClient
	def pollIntervalMillis
	def timeoutMillis
	def buildNumber
	def log = LogFactory.getLog(JenkinsRunner)
	def commanderClient

	JenkinsRunner(jenkinsClient, pollIntervalMillis = 15000, timeoutMillis = 1800000) {
		this.jenkinsClient = jenkinsClient
		this.pollIntervalMillis = pollIntervalMillis
		this.timeoutMillis = timeoutMillis
		this.commanderClient = new CommanderClient()
	}

	def runJob(jobName, jobParams = null) {
		if (jobParams) {
			buildNumber = jenkinsClient.startJobWithParams(jobName, jobParams)
			log.info("Started build " + buildNumber + " of jenkins job " + jobName + " with parameters " + jobParams)
		} else {
			buildNumber = jenkinsClient.startJob(jobName)
			log.info("Started build " + buildNumber + " of jenkins job " + jobName)
		}
		def start = System.currentTimeMillis()
		def end  = start + timeoutMillis
		while(!jenkinsClient.isFinished(jobName, buildNumber)) {
			log.info("Jenkins job not complete yet, retrying in ${pollIntervalMillis} ms <${getJobUrl(jobName, buildNumber)}>")
			sleep(pollIntervalMillis)
			if (System.currentTimeMillis() > end) {
				def message = "Timed out after ${timeoutMillis} ms waiting for job to finish <${getJobUrl(jobName, buildNumber)}>"
				log.error(message)
				throw new JenkinsException(message)
			}
		}
		if (jenkinsClient.isSuccessful(jobName, buildNumber)) {
			log.info("Jenkins job completed successfully <${getJobUrl(jobName, buildNumber)}>")
		} else {
            def message = "Jenkins job failed <${getJobUrl(jobName, buildNumber)}>: Console log: ${jenkinsClient.getConsole(jobName, buildNumber)}"
            log.error(message)
            throw new JenkinsException(message)
        }
		jenkinsClient.addDescription(jobName, buildNumber, getECJobDescription())
	}

	def getJobUrl(jobName, buildNumber) {
		jenkinsClient.getJobUrl(jobName, buildNumber)
	}

	def getECJobDescription() {
        """
            Running on behalf of EC UserID - ${commanderClient.getUserId()?.toString()} and
            <a href='https://commander.gapinc.dev/commander/link/jobDetails/jobs/${commanderClient.getJobId()}'>triggered by this EC Job</a>
        """
	}
}
