package com.gap.gradle.jenkins

import groovyx.net.http.HTTPBuilder
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST

class JenkinsClient {

    def http
    def userName
    def apiToken
    private Object serverUrl
    Log log = LogFactory.getLog(JenkinsClient)

    JenkinsClient(serverUrl, userName, apiToken) {
        this.serverUrl = serverUrl
        this.http = new HTTPBuilder(serverUrl)
        this.userName = userName
        this.apiToken = apiToken
    }

    def getAuthorizationHeader(){
        def credentials = "${userName}:${apiToken}".toString().bytes.encodeBase64().toString()
        "Basic  ${credentials}"
    }

    def getJobUrl(jobName, buildNumber) {
        "${serverUrl}/job/${jobName}/${buildNumber}".toString()
    }

    def getNextBuildNumber (jobName){
        http.request(GET,JSON) {
            uri.path = "/job/${jobName}/api/json"
            headers.'Authorization' = getAuthorizationHeader()
            response.success = {resp, json ->
                json.nextBuildNumber
            }
            response.failure = {resp ->
                throw new JenkinsException("Failed to get the next build number from Jenkins: ${resp.statusLine}")
            }
        }
    }

    def startJob(jobName) {
        def nextBuildNumber = getNextBuildNumber (jobName)

        http.request(POST) {
            uri.path = "/job/${jobName}/build"
            headers.'Authorization' = getAuthorizationHeader()
            response.success = {
                nextBuildNumber
            }
            response.failure = { resp ->
                throw new JenkinsException("Failed to start job in jenkins: ${resp.statusLine}")
           }
        }
    }

	def startJobWithParams(jobName, jobParams) {
		def nextBuildNumber = getNextBuildNumber (jobName)
		http.request(POST) {
			uri.path = "/job/${jobName}/buildWithParameters"
			headers.'Authorization' = getAuthorizationHeader()
			requestContentType = URLENC
			body =  jobParams
			response.success = {
				nextBuildNumber
			}
			response.failure = { resp ->
				throw new JenkinsException("Failed to start job in jenkins: ${resp.statusLine}")
		   }
		}
	}

    def isFinished(jobName, buildNumber) {
        def jobStatus = getJobStatus(jobName, buildNumber)
        jobStatus == JobStatus.success || jobStatus == JobStatus.failure
    }

	def addDescription(jobName, buildNumber, description) {
		http.request(POST) {
			uri.path = "/job/${jobName}/${buildNumber}/submitDescription" 
			headers.'Authorization' = getAuthorizationHeader()
			requestContentType = URLENC
			body = ['description': description]
			response.success = {}
			response.failure = {}
		}
	}

    private def getJobStatus(jobName, buildNumber) {
        http.request(GET, JSON) {
            uri.path = "/job/${jobName}/${buildNumber}/api/json"
            headers.'Authorization' = getAuthorizationHeader()
            response.success = { resp, json ->
                if (json.building) {
                    return JobStatus.pending
                }
                json.result == 'SUCCESS'? JobStatus.success : JobStatus.failure
            }
            response.failure = { resp ->
                if (resp.statusLine.statusCode == 404) {
                    JobStatus.unknown
                } else {
                    log.info(resp)
                    throw new JenkinsException("Unable to get status of build ${buildNumber} for job ${jobName}")
                }
            }
        }
    }

    def isSuccessful(jobName, buildNumber) {
        def jobStatus = getJobStatus(jobName, buildNumber)
        jobStatus == JobStatus.success
    }

    def getConsole(jobName, buildNumber) {
        http.request(GET, TEXT) {
            uri.path = "/job/${jobName}/${buildNumber}/logText/progressiveText"
            response.success = {resp, reader ->
                reader.text
            }

            response.failure = {resp ->
                throw new JenkinsException("Unable to get console log for build ${buildNumber} for job ${jobName}. Error - ${resp.statusLine}")
            }
        }
    }
}