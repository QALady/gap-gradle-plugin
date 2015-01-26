package com.gap.pipeline

import com.gap.pipeline.ec.CommanderClient

class IvyConfig {
	CommanderClient ecclient = new CommanderClient()
    def url = "http://artifactory.gapinc.dev/artifactory/local-non-prod"
    def userName = "${ecclient.getArtifactoryUserName()}"
    def password = "${ecclient.getArtifactoryPassword()}"
    def checkIfExists = false
}
