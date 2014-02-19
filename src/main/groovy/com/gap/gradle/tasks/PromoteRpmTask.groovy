package com.gap.gradle.tasks

import org.gradle.api.Project

import com.gap.gradle.yum.YumClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.RequiredParameters
import com.gap.pipeline.tasks.annotations.Require

@RequiredParameters([
    @Require(parameter='prodDeploy.yumSourceUrl', description='name of repo within gapSoftware that hold rpm'),
    @Require(parameter='prodDeploy.rpmName', description='full name of rpm file, including version architecture and extension'),
    @Require(parameter='prodDeploy.yumDestinationUrl', description='url of the yum prod repo to upload the rpm to'),
    @Require(parameter='prodDeploy.appVersion', description='app version of the rpm'),
] )
class PromoteRpmTask extends WatchmenTask{

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
        if(!project.prodDeploy.rpmName.contains('.rpm')){
            throw new IllegalArgumentException("rpmConfig.rpmName ${project.prodDeploy.rpmName} does not have .rpm extension")
        }
        if(!project.prodDeploy.rpmName.contains(project.prodDeploy.appVersion)){
            throw new IllegalArgumentException("rpmConfig.rpmName ${project.prodDeploy.rpmName} does not contain app version ${project.prodDeploy.appVersion}")
        }
    }

    def execute(){
        validate()
        def copyToLocation = project.buildDir.path + '/tmp'

        yumClient.downloadRpm(project.prodDeploy.yumSourceUrl, project.prodDeploy.rpmName, copyToLocation)
        yumClient.uploadRpm(project.prodDeploy.rpmName, copyToLocation, project.prodDeploy.yumDestinationUrl)
        yumClient.recreateYumRepo(project.prodDeploy.yumDestinationUrl)
    }

}
