package com.gap.pipeline.tasks

import com.gap.pipeline.ec.CommanderArtifacts
import org.apache.commons.logging.LogFactory

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

@RequiredParameters([
@Require(parameter = 'artifactCoordinates', description = "location of artifacts, to pass to prodDeploy job"),
@Require(parameter = 'tagMessageComment', description = "Comments provided by Release Engineer"),
@Require(parameter = 'ticketId', description = "ServiceCenter Task Id"),
@Require(parameter = 'userId', description = "User ID of person triggering deployToProd EC job"),
@Require(parameter = 'userName', description = "User Name of person triggering deployToProd EC job"),
@Require(parameter = 'startTime', description = "Start time triggering deployToProd EC job")

])
class GenerateAuditReportTask extends WatchmenTask {
    def log = LogFactory.getLog(GenerateAuditReportTask)
    def ecUserId
    def ecUserName
    def ecStartTime
    def ecComment
    def ecServiceTicketId
    def project
    def ecArtifactCoordinates

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
        ecComment = project.tagMessageComment
        log.info("Comment - " + ecComment)
        ecServiceTicketId = project.ticketId
        log.info("Service Ticket - " + ecServiceTicketId)
        ecArtifactCoordinates = project.artifactCoordinates
        log.info("Artifact co-ordinate - " + ecArtifactCoordinates)

        createChangelistFile()
        copyArtifactsForUseByEC()
        publishArtifactLinksToEC()
    }

    def createChangelistFile(){
        File auditReport = new File("${project.buildDir}/reports/Audit_Report.txt")
        def writer = auditReport.newWriter()
        writer.append(
        """
            ***********************************************************************************************
            *                                                                                             *
            *                              Production Deployment Audit Report                             *
            *                                                                                             *
            ***********************************************************************************************
            *                                                                                             *
            *   EC UserID - ${ecUserId}                                                                   *
            *   EC UserName - ${ecUserName}                                                               *
            *   Job Start Time - ${ecStartTime}                                                           *
            *   Comments - ${ecComment}                                                                   *
            *   Service Ticket - ${ecServiceTicketId}                                                     *
            *   Artifact Co-ordinate - ${ecArtifactCoordinates}                                           *
            *                                                                                             *
            ***********************************************************************************************
        """
        )
        log.info("File is in - " + auditReport.absolutePath)
        writer.close()

    }

    private void copyArtifactsForUseByEC () {
        new CommanderArtifacts(new CommanderClient()).copyToArtifactsDir("${project.buildDir}/reports/Audit_Report.txt")
    }


    private void publishArtifactLinksToEC() {
        def artifacts = new CommanderArtifacts(new CommanderClient());
        artifacts.publishLinks()
    }
}