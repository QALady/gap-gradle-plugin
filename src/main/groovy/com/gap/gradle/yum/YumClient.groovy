package com.gap.gradle.yum

import com.gap.gradle.utils.ShellCommand
import com.sun.org.apache.commons.logging.LogFactory




class YumClient {
    def shellCommand
    def log = LogFactory.getLog(YumClient)

    YumClient() {
        this.shellCommand = new ShellCommand()
    }

    YumClient(ShellCommand shellCommand) {
        this.shellCommand = shellCommand
    }

    void downloadRpm(String repoUrl, String rpmName, String destination) {
        log.info("downloading \${repoUrl}/\${rpmName} to \${destination}")
        this.shellCommand.execute("curl -o ${destination}/${rpmName} --create-dirs --fail ${repoUrl}/${rpmName}")
    }

    void uploadRpm(String rpmName, String rpmLocation, String yumDestinationUrl){
        def yumRepo = parse(yumDestinationUrl)
        log.info("copying ${rpmName} to ${yumRepo.hostname}:${yumRepo.path}/${rpmName}\"")
        this.shellCommand.execute("scp ${rpmLocation}/${rpmName} ${yumRepo.hostname}:${yumRepo.path}/${rpmName}")
    }

    void recreateYumRepo(String yumDestinationUrl) {
        def yumRepo = parse(yumDestinationUrl)
        this.shellCommand.execute(["ssh", yumRepo.hostname, "sudo createrepo --database --update ${yumRepo.path}".toString()])
    }

    def parse(String url) {
        def matcher = (url =~ /http:\/\/([^\/]+)\/(.+)/)
        [hostname: matcher[0][1], path: '/mnt/repos/' + matcher[0][2]]
    }

}
