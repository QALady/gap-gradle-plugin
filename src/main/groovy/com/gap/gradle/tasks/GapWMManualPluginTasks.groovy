package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask

class GapWMManualPluginTasks extends WatchmenTask {

	Project project
	CommanderClient commanderClient
	ShellCommand shellCommand
	def logger = LogFactory.getLog(com.gap.gradle.tasks.GapWMManualPluginTasks)
	
	GapWMManualPluginTasks(Project project, commanderClient = new CommanderClient(), shellCommand = new ShellCommand()) {
		super(project)
		this.project = project
		this.commanderClient = commanderClient
		this.shellCommand = shellCommand
	}

	public void executeCreateLinksForApprovalAndRejection() {
		try {
			shellCommand.execute(["ectool", "setProperty", """/myJob/report-urls/Approve""", "" + commanderClient.getBaseUrl() + "commander/runProcedure.phprunNow=1&projectName=" + commanderClient.getCurrentProjectName() + "&procedureName=Set%20approval%20status%20property&numParameters=2&parameters1_name=jobId&parameters1_value=" + commanderClient.getJobId() + "&parameters2_name=approvalStatus&parameters2_value=approved"])
			} catch(ShellCommandException se) {
            if(se.getMessage().contains('ectool error [InvalidCredentialName]')) {
            logger.warn("WARNING: Using dummy credentials - Only WM Gradle:Invoke & WM Exec:Run are approved steps to access artifactory credentials. This will not impact your job unless you are trying to use the Artifactory credentials in this step")
            } else {
              throw se
            }
          }

          try {
            shellCommand.execute(["ectool", "setProperty", """/myJob/report-urls/Reject""", """/server/baseUrl/commander/runProcedure.php?runNow=1&projectName=/myJobStep/projectName/&procedureName=Set%20approval%20status%20property&numParameters=2&parameters1_name=jobId&parameters1_value=/myJob/jobId/&parameters2_name=approvalStatus&parameters2_value=rejected"""])
		  } catch(ShellCommandException se) {
			if(se.getMessage().contains('ectool error [InvalidCredentialName]')) {
			logger.warn("WARNING: Using dummy credentials - Only WM Gradle:Invoke & WM Exec:Run are approved steps to access artifactory credentials. This will not impact your job unless you are trying to use the Artifactory credentials in this step")
			} else {
			  throw se
			}
		  }
	}
	
	public void executeRemoveApprovalAndRejectionLinks() {
		try {
			shellCommand.execute(["ectool", "deleteProperty", """/myJob/report-urls/Approve"""])
          } catch(ShellCommandException se) {
            if(se.getMessage().contains('ectool error [InvalidCredentialName]')) {
            logger.warn("WARNING: Using dummy credentials - Only WM Gradle:Invoke & WM Exec:Run are approved steps to access artifactory credentials. This will not impact your job unless you are trying to use the Artifactory credentials in this step")
            } else {
              throw se
            }
          }

          try {
            shellCommand.execute(["ectool", "deleteProperty", """/myJob/report-urls/Reject"""])
		  } catch(ShellCommandException se) {
			if(se.getMessage().contains('ectool error [InvalidCredentialName]')) {
			logger.warn("WARNING: Using dummy credentials - Only WM Gradle:Invoke & WM Exec:Run are approved steps to access artifactory credentials. This will not impact your job unless you are trying to use the Artifactory credentials in this step")
			} else {
			  throw se
			}
		  }
	}
	
	public void executeFailorPassBasedOnProperty() {
		def approvalStatus = commanderClient.getECProperty("/myJob/approvalStatus")
		def wmManualjobId = commanderClient.getJobId()

		if ("approved".equalsIgnoreCase(approvalStatus)) {
			logger.info("Job is approved \n")
		}else {
			logger.info("Job is rejected \n")
			shellCommand.execute(["ectool", "abortJob", wmManualjobId ,"""--force 1"""])
        }
	}
	
}
