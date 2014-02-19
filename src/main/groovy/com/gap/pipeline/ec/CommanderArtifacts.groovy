package com.gap.pipeline.ec

import com.gap.pipeline.utils.ShellCommand

class CommanderArtifacts {

    CommanderClient client

    CommanderArtifacts(CommanderClient client) {
        this.client = client
    }

    void publishLinks() {
        def jobDirectory = client.currentJobDir
        def artifactsDir = "${jobDirectory}/artifacts"
        def files = new File(artifactsDir).listFiles()
        for(def file : files){
           client.addLink(file.getName(), client.getJobId())
        }
    }

    def copyToArtifactsDir(String artifactPath) {
        def jobDirectory = client.currentJobDir
        def destinationDirectory = "${jobDirectory}/artifacts"
        new File(destinationDirectory).mkdir()
        new ShellCommand().execute("cp ${artifactPath} ${destinationDirectory}")
    }
}
