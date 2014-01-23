package com.gap.gradle.plugins

class JenkinsConfig {

    def serverUrl = System.getenv("JENKINS_URL")
    def user = System.getenv("JENKINS_USER")
    def authToken = System.getenv("JENKINS_AUTH_TOKEN")
}
