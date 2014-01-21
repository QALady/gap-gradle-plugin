package com.gap.gradle.jenkins

import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

import groovyx.net.http.HTTPBuilder
enum JobStatus
{
    success,
    failure,
    pending,
    unknown
}

class JenkinsClient {

    def http
    def userName
    def apiToken

    JenkinsClient(serverUrl, userName, apiToken) {
        this.http = new HTTPBuilder(serverUrl)
        this.userName = userName
        this.apiToken = apiToken
    }

    def getAuthorizationHeader(){
        def credentials = "${userName}:${apiToken}".toString().bytes.encodeBase64().toString()
        "Basic  ${credentials}"
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

    def isFinished(jobName, jobNumber) {
        def jobStatus = getJobStatus(jobName, jobNumber)
        jobStatus == JobStatus.success || jobStatus == JobStatus.failure
    }

    private def getJobStatus(jobName, jobNumber) {
        http.request(GET, JSON) {
            uri.path = "/job/${jobName}/${jobNumber}/api/json"
            headers.'Authorization' = getAuthorizationHeader()
            response.success = { resp, json ->
                if (json.building)
                    return JobStatus.pending
                json.result == 'SUCCESS'? JobStatus.success : JobStatus.failure
            }
            response.failure = { resp ->
                if (resp.statusLine.statusCode == 404)
                    JobStatus.unknown
                else
                    throw new JenkinsException("Unable to get status of build ${jobNumber} for job ${jobName}")
            }
        }
    }

    def isSuccessful(jobName, jobNumber) {
        def jobStatus = getJobStatus(jobName, jobNumber)
        jobStatus == JobStatus.success
    }

    def getConsole(jobName, jobNumber) {

    }
}