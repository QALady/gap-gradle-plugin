package com.gap.pipeline.tasks

import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import com.gap.pipeline.yum.YumClient
import org.gradle.api.Project

@RequiredParameters([
    @Require(parameter='rpmConfig.yumSourceUrl', description='name of repo within gapSoftware that hold rpm'),
    @Require(parameter='rpmConfig.rpmName', description='full name of rpm file, including version architecture and extension'),
    @Require(parameter='rpmConfig.destination', description='directory to download rpm to, relative to directory in which task is run'),
    @Require(parameter='appVersion', description='app version of the rpm'),
    @Require(parameter='rpmConfig.prodDestionUrl', description='hostname of the prod yum repo'),
    @Require(parameter='rpmConfig.prodPath', description='relative path of the rpm\'s inside the prod yum repo'),
    @Require(parameter='rpmConfig.channel', description='channel of the rpm')
] )
class PromoteRpmTask extends com.gap.pipeline.tasks.WatchmenTask{

    Project project
    YumClient yumClient

    PromoteRpmTask(Project project, YumClient yumClient) {
        super(project)
        this.project = project
        this.yumClient = yumClient
    }

    PromoteRpmTask(project){
        super(project)
        this.project = project
        this.yumClient = new YumClient()
    }

    def validate(){
        if(!project.rpmConfig.rpmName.contains('.rpm')){
            throw new IllegalArgumentException("rpmConfig.rpmName ${project.rpmConfig.rpmName} does not have .rpm extension")
        }
        if(!project.rpmConfig.rpmName.contains(project.rpmConfig.appVersion)){
            throw new IllegalArgumentException("rpmConfig.rpmName ${project.rpmConfig.rpmName} does not contain app version ${project.rpmConfig.appVersion}")
        }
    }

    def execute(){
        validate()
        yumClient.downloadRpm(project.rpmConfig.repoUrl, project.rpmConfig.rpmName, project.rpmConfig.destination)
        yumClient.uploadRpm(project.rpmConfig.rpmName, project.rpmConfig.destination, project.rpmConfig.prodHostname,
                        project.rpmConfig.prodPath, project.rpmConfig.channel)
        yumClient.recreateYumRepo(project.rpmConfig.prodHostname, project.rpmConfig.prodPath, project.rpmConfig.channel)
    }

}
