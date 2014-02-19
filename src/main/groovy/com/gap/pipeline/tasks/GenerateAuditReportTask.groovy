package com.gap.pipeline.tasks

import com.gap.pipeline.ec.CommanderArtifacts
import org.apache.commons.logging.LogFactory

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

@RequiredParameters([
@Require(parameter = 'prodPrepare.sha1Ids', description = "SHA ID's of the chef objects to promote"),
@Require(parameter = 'artifactCoordinates', description = "location of artifacts, to pass to prodDeploy job"),
@Require(parameter = 'prodPrepare.appVersion', description = "Version of the application that should be deployed in the prod node."),
@Require(parameter = 'prodPrepare.cookbookSha1Id', description = "Application promoted cookbook sha1Id in git"),
@Require(parameter = 'prodPrepare.cookbookName', description = "Application cookbook name"),
@Require(parameter = 'userId', description = "User ID of person triggering deployToProd EC job"),
@Require(parameter = 'userName', description = "User Name of person triggering deployToProd EC job"),
@Require(parameter = 'startTime', description = "Start time triggering deployToProd EC job")

])
class GenerateAuditReportTask extends WatchmenTask {
    def log = LogFactory.getLog(GenerateAuditReportTask)
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
    def project

    GenerateAuditReportTask(project){
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

        createChangelistFile()
        copyArtifactsForUseByEC()
        publishArtifactLinksToEC()
    }

    def createChangelistFile(){
        new File("${project.buildDir}/reports").mkdirs()
        File auditReport = new File("${project.buildDir}/reports/auditReport.txt")
        def writer = auditReport.newWriter()

        writer.append("***********************************************************************************************\n");
        writer.append("*                                                                                             *\n");
        writer.append("                               Production Deployment Audit Report                              \n");
        writer.append("*                                                                                             *\n");
        writer.append("***********************************************************************************************\n");

        writer.append("EC UserID - " + ecUserId + "\n")
        writer.append("EC UserName - " + ecUserName + "\n")
        writer.append("Job Start Time - " + ecStartTime + "\n")
        writer.append("ChefObjects ShaIds - " + sha1Ids + "\n")
        writer.append("Chef RoleName - " + roleName + "\n")
        writer.append("Chef CookbookName - " + cookbookName + "\n")
        writer.append("Chef Cookbook ShaId - " + cookbookSha1Id + "\n")
        writer.append("ApplicationNode - " + nodes + "\n")
        writer.append("IsRPMApplication - " + isRPM + "\n")
        writer.append("ApplicationVersion - " + appVersion)

        log.info("File is in - " + auditReport.absolutePath)
        writer.close()

    }

    private void copyArtifactsForUseByEC () {
        new CommanderArtifacts(new CommanderClient()).copyToArtifactsDir("${project.buildDir}/reports/*")
    }


    private void publishArtifactLinksToEC() {
        def artifacts = new CommanderArtifacts(new CommanderClient());
        artifacts.publishLinks()
    }
}