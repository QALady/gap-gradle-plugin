package com.gap.pipeline

import com.gap.pipeline.ec.CommanderClient

class IvyConfig {
    private String userName
    private String password

	CommanderClient ecclient = new CommanderClient()
    def url = "http://artifactory.gapinc.dev/artifactory/local-non-prod"
    def checkIfExists = false

    def getUserName() {
        if (userName == null) {
            userName = ecclient.getArtifactoryUserName()
        }
        userName
    }

    def getPassword() {
        if (password == null) {
            password = ecclient.getArtifactoryPassword()
        }
        password
    }

    void setUserName(String userName) {
        this.userName = userName
    }

    void setPassword(String password) {
        this.password = password
    }
}
