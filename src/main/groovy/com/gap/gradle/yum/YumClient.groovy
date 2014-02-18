package com.gap.gradle.yum

import com.gap.gradle.utils.ShellCommand




class YumClient {
    def shellCommand

    YumClient() {
        this.shellCommand = new ShellCommand()
    }

    YumClient(ShellCommand shellCommand) {
        this.shellCommand = shellCommand
    }

    void downloadRpm(String repoUrl, String rpmName, String destination) {
        this.shellCommand.execute("curl -o ${destination}/${rpmName} --create-dirs ${repoUrl}/${rpmName}")
    }

    void uploadRpm(String rpmName, String rpmLocation, String prodHostname, String prodPath, String channel){
        this.shellCommand.execute("scp ${rpmLocation}/${rpmName} ${prodHostname}:${prodPath}/${channel}/${rpmName}")
    }

    void recreateYumRepo(String prodHostname, String prodPath, String channel) {
        this.shellCommand.execute(["ssh", prodHostname, "sudo createrepo --database --update ${prodPath}/${channel}".toString()])
    }
}
