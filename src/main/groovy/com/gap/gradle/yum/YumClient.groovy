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

    void uploadRpm(String rpmName, String rpmLocation, String yumDestinationUrl){
        def yumRepo = parse(yumDestinationUrl)
        this.shellCommand.execute("scp ${rpmLocation}/${rpmName} ${yumRepo.hostname}:${yumRepo.path}/${rpmName}")
    }

    def parse(String url) {
        def matcher = (url =~ /http:\/\/([^\/]+)\/(.+)/)
        [hostname: matcher[0][1], path: '/mnt/repos/' + matcher[0][2]]
    }

    void recreateYumRepo(String yumDestinationUrl) {
        def yumRepo = parse(yumDestinationUrl)
        this.shellCommand.execute(["ssh", yumRepo.hostname, "sudo createrepo --database --update ${yumRepo.path}".toString()])
    }


}
