package com.gap.gradle.tasks

import com.gap.gradle.yum.YumClient
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

@RequiredParameters([
    @Require(parameter='rpm.yumSourceUrl', description='name of repo within gapSoftware that hold rpm'),
    @Require(parameter='rpm.rpmNames', description='full name of rpm file, including version architecture and extension'),
    @Require(parameter='rpm.yumDestinationUrl', description='url of the yum prod repo to upload the rpm to'),
    @Require(parameter='rpm.appVersion', description='app version of the rpm'),
] )
class PromoteRpmsTask extends WatchmenTask{

    Project project
    YumClient yumClient
    def log = LogFactory.getLog(PromoteRpmsTask)


    PromoteRpmsTask(Project project, YumClient yumClient) {
        super(project)
        this.project = project
        this.yumClient = yumClient
    }

    PromoteRpmsTask(project){
        super(project)
        this.project = project
        this.yumClient = new YumClient()
    }

    def validate(){
        super.validate()
        for(def rpm: project.rpm.rpmNames){
            if(!rpm.contains('.rpm')){
                throw new IllegalArgumentException("rpm.rpmNames ${rpm} does not have .rpm extension")
            }
            if(!rpm.contains(project.rpm.appVersion)){
                throw new IllegalArgumentException("rpm.rpmNames ${rpm} does not contain app version ${project.rpm.appVersion}")
            }
        }
    }

    def execute(){
        validate()
        def copyToLocation = project.buildDir.path + '/tmp'
        for(def rpm: project.rpm.rpmNames){
            yumClient.downloadRpm(project.rpm.yumSourceUrl, rpm, copyToLocation)
        }
        for(def rpm: project.rpm.rpmNames){
            yumClient.uploadRpm(rpm, copyToLocation, project.rpm.yumDestinationUrl)
        }
        yumClient.recreateYumRepo(project.rpm.yumDestinationUrl)
    }

}
