package com.gap.pipeline.tasks

import com.gap.pipeline.ec.CommanderArtifacts
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.apache.commons.logging.LogFactory

@RequiredParameters([
@Require(parameter = 'prodPrepare.sha1Ids', description = "SHA ID's of the chef objects to promote"),
@Require(parameter = 'prodPrepare.appVersion', description = "Version of the application that should be deployed in the prod node."),
@Require(parameter = 'prodPrepare.cookbookSha1Id', description = "Application promoted cookbook sha1Id in git"),
@Require(parameter = 'prodPrepare.cookbookName', description = "Application cookbook name"),
@Require(parameter = 'userId', description = "User ID of person triggering deployToProd EC job"),
@Require(parameter = 'userName', description = "User Name of person triggering deployToProd EC job"),
@Require(parameter = 'startTime', description = "Start time triggering deployToProd EC job")

])
class GenerateChangeListReportTask extends WatchmenTask {
    def log = LogFactory.getLog(GenerateChangeListReportTask)
    def ecUserId
    def ecUserName
    def ecStartTime
    def sha1Ids
    def roleName
    def cookbookName
    def cookbookSha1Id
    def nodes
    def isRPM
    def appVersion
    def rpmVersion
    def project

    GenerateChangeListReportTask(project){
        super(project)
        this.project = project
    }

    public void execute(){

        log.info("Executing GenerateChangeList task ...")
        def commanderClient = new CommanderClient()
        ecUserId = commanderClient.getUserId().toString()
        log.info("UserID from EC - " + ecUserId)
        ecUserName = commanderClient.getUserName().toString()
        log.info("UserName from EC - " + ecUserName)
        ecStartTime = commanderClient.getStartTime().toString()
        log.info("StartTime from EC - " + ecStartTime)
        sha1Ids = project.prodPrepare.sha1Ids
        log.info("Sha1 ID(s) - " + sha1Ids)
        roleName = project.prodPrepare.roleName
        log.info("Role Name - " + roleName)
        cookbookName = project.prodPrepare.cookbookName
        log.info("Cookbook Name - " + cookbookName)
        cookbookSha1Id = project.prodPrepare.cookbookSha1Id
        log.info("Cookbook Sha1- " + cookbookSha1Id)
        nodes = project.prodPrepare.nodes
        log.info("Deployment Nodes - " + nodes)
        isRPM = project.prodPrepare.isRPM
        log.info("Is RPM deploy? - " + isRPM)
        appVersion = project.prodPrepare.appVersion
        log.info("Application deploy version - " + appVersion)
        rpmVersion = project.prodPrepare.rpmName
        log.info("Application deploy version - " + rpmVersion)

        createChangelistFile()
        copyArtifactsForUseByEC()
        publishArtifactLinksToEC()
    }

    def createChangelistFile(){
        File changeListReport = new File("${project.buildDir}/reports/ChangeList_Report.txt")
        def writer = changeListReport.newWriter()

        """
            ***********************************************************************************************
            *                                                                                             *
            *                              Production Deployment ChangeList Report                        *
            *                                                                                             *
            ***********************************************************************************************
            *                                                                                             *
            *  EC UserID - ${ecUserId}                                                                    *
            *  EC UserName - ${ecUserName}                                                                *
            *  Job Start Time - ${ecStartTime}                                                            *
            *  ChefObjects ShaIds - ${sha1Ids}                                                            *
            *  Chef RoleName - ${roleName}                                                                *
            *  Chef CookbookName - ${cookbookName}                                                        *
            *  Chef Cookbook ShaId - ${cookbookSha1Id}                                                    *
            *  Application Node - ${nodes}                                                                *
            *  RPM Artifact? - ${isRPM}                                                                   *
            *  Application Version - ${appVersion}                                                        *
            *  RPM Version - ${rpmVersion}                                                                *
            *                                                                                             *
            ***********************************************************************************************
        """

        log.info("ChangeListReport is in - " + changeListReport.absolutePath)
        writer.close()

    }

    private void copyArtifactsForUseByEC () {
        new CommanderArtifacts(new CommanderClient()).copyToArtifactsDir("${project.buildDir}/reports/ChangeList_Report.txt")
    }


    private void publishArtifactLinksToEC() {
        def artifacts = new CommanderArtifacts(new CommanderClient());
        artifacts.publishLinks()
    }
}