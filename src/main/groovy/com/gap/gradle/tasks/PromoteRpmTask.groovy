package com.gap.gradle.tasks

import org.gradle.api.Project

import com.gap.gradle.yum.YumClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.RequiredParameters
import com.gap.pipeline.tasks.annotations.Require

@RequiredParameters([
    @Require(parameter='rpm.yumSourceUrl', description='name of repo within gapSoftware that hold rpm'),
    @Require(parameter='rpm.rpmName', description='full name of rpm file, including version architecture and extension'),
    @Require(parameter='rpm.yumDestinationUrl', description='url of the yum prod repo to upload the rpm to'),
    @Require(parameter='rpm.appVersion', description='app version of the rpm'),
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
        if(!project.rpm.rpmName.contains('.rpm')){
            throw new IllegalArgumentException("rpm.rpmName ${project.rpm.rpmName} does not have .rpm extension")
        }
        if(!project.rpm.rpmName.contains(project.rpm.appVersion)){
            throw new IllegalArgumentException("rpm.rpmName ${project.rpm.rpmName} does not contain app version ${project.rpm.appVersion}")
        }
    }

    def execute(){
        validate()
        def copyToLocation = project.buildDir.path + '/tmp'

        yumClient.downloadRpm(project.rpm.yumSourceUrl, project.rpm.rpmName, copyToLocation)
        yumClient.uploadRpm(project.rpm.rpmName, copyToLocation, project.rpm.yumDestinationUrl)
        yumClient.recreateYumRepo(project.rpm.yumDestinationUrl)
    }

}
