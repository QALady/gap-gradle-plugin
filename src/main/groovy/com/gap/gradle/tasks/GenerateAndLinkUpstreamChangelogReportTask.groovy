package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.Property
import com.gap.pipeline.tasks.WatchmenTask

class GenerateAndLinkUpstreamChangelogReportTask extends WatchmenTask {
	Project project
	ShellCommand shellCommand = new ShellCommand()
	CommanderClient commanderClient = new CommanderClient(shellCommand)
	def logger = LogFactory.getLog(com.gap.gradle.tasks.GenerateAndLinkUpstreamChangelogReportTask)

	public GenerateAndLinkUpstreamChangelogReportTask(Project project) {
		super(project);
		this.project = project;
	}

	public def execute() {
		validate()
		def upstreamJobId = getUpstreamJobId()
		println "UPSTREAM Job ID: " + upstreamJobId
		createChangelistFile(getECPropertySheet(upstreamJobId))
	}

	def getUpstreamJobId() {
		// get the Upstream Job report-url property and extract upstream JobID.
		def upStreamJob = commanderClient.getReportUrlPropertyValue("Upstream Job")
		println "upstream job: " + upStreamJob
		def upStreamJobId = upStreamJob.tokenize("/").last()
		println "upstream job id after split: " + upStreamJobId
		return upStreamJobId		
	}

	def getECPropertySheet(upstreamJobId) {
		def prop
		try{
			prop = shellCommand.execute(['ectool', 'getProperties', '--path', '/jobs[$upstreamJobId]/ecscm_changeLogs', '--recurse', '1'])
		}
		catch (ShellCommandException e) {
			if(e.message.contains('[NoSuchProperty]')){
				logger.debug("Requested property does not exist. ${e.message}\n")
				return Property.invalidProperty(key)
			}
			else throw e
		}
		println "Change Log from Upstream: " + prop
		return new XmlSlurper().parseText(prop)
	}

	def createChangelistFile(upstreamEcscmChangeLogs) {
		File changeListReport = new File("${project.buildDir}/reports/UpStream_ChangeList_Report.txt")
		def writer = changeListReport.newWriter()
		writer.append(
		"""
            ***********************************************************************************************
            *                                                                                             *
            *                              Upstream ChangeList Report                        			  *
            *                                                                                             *
            ***********************************************************************************************
		""")
		upstreamEcscmChangeLogs.propertySheet.property.each { p ->
			writer.append(p.propertyId)
			writer.append(p.propertyName)
			writer.append(p.value)
		}
		writer.append(
		"""
            ***********************************************************************************************
        """
		)
		log.info("ChangeListReport is in - " + changeListReport.absolutePath)
		writer.close()
	}
}
