package com.gap.gradle.jenkins

import static groovyx.net.http.Method.*
import static groovyx.net.http.ContentType.*

import groovyx.net.http.HTTPBuilder

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
        }
    }

    def startJob(jobName) {
        http.request(POST) {
            uri.path = "/job/${jobName}/build"
            headers.'Authorization' = getAuthorizationHeader()
            response.success = {
                getNextBuildNumber (jobName)
            }
            response.failure = { resp ->
                throw new JenkinsException("Failed to start job in jenkins: ${resp.statusLine}")
           }
        }

//        return buildNumber
    }

    def isFinished(jobName, jobNumber) {
        return true
    }

    def isSuccessful(jobName, jobNumber) {

    }

    def getConsole(jobName, jobNumber) {

    }
}