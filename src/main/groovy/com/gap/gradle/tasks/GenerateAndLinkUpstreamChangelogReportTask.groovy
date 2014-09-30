package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException
import com.gap.pipeline.ec.CommanderArtifacts
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.Property
import com.gap.pipeline.tasks.WatchmenTask

class GenerateAndLinkUpstreamChangelogReportTask extends WatchmenTask {
	Project project
	ShellCommand shellCommand = new ShellCommand()
	CommanderClient commanderClient = new CommanderClient(shellCommand)
	def logger = LogFactory.getLog(com.gap.gradle.tasks.GenerateAndLinkUpstreamChangelogReportTask)
	def upstream_changelog_file = "${project.projectDir}/UpStream_ChangeList_Report.txt"

	public GenerateAndLinkUpstreamChangelogReportTask(Project project) {
		super(project);
		this.project = project;
	}

	public def execute() {
		validate()
		def upstreamJobId = "290af17d-47ab-11e4-b16c-00505625f614"//getUpstreamJobId() // TODO: hardcoded for now for testing in pipeline.
		if (upstreamJobId) {
			println "UPSTREAM Job ID: " + upstreamJobId
			createChangelistFile(getECPropertySheet(upstreamJobId))
			copyArtifactsForUseByEC()
			publishArtifactLinksToEC()
		} else {
			logger.info("Upstream Job id is not linked to this downstream job. No upstream segments changed to have triggered this downstream job.")
		}
	}

	def getUpstreamJobId() {
		// get the Upstream Job report-url property and extract upstream JobID.
		Property upStreamJobProperty = commanderClient.getReportUrlProperty("Upstream Job")
		if (upStreamJobProperty.isValid()) {
			def upStreamJob = upStreamJobProperty.value
			println "upstream job: " + upStreamJob
			def upStreamJobId = upStreamJob.tokenize("/").last()
			println "upstream job id after split: " + upStreamJobId
			return upStreamJobId		
		}
		return null
	}

	def getECPropertySheet(upstreamJobId) {
		def prop
		try{
			prop = shellCommand.execute(['ectool', 'getProperties', '--path', "/jobs[$upstreamJobId]/ecscm_changeLogs", '--recurse', '1'])
		}
		catch (ShellCommandException e) {
			if(e.message.contains('[NoSuchProperty]')){
				logger.debug("Requested property does not exist. ${e.message}\n")
				return Property.invalidProperty(key)
			}
			else throw e
		}
		println "Change Log from Upstream: " + prop
		def response = new XmlSlurper().parseText(prop)
		return response.propertySheet.property // return all the changelog property records.
	}

	def createChangelistFile(upstreamEcscmChangeLogs) {
		File changeListReport = new File(upstream_changelog_file)
		def writer = changeListReport.newWriter()
		writer.append(
		"""
            ***********************************************************************************************
            *                                                                                             *
            *                              Upstream ChangeList Report                        			  *
            *                                                                                             *
            ***********************************************************************************************
		""")
		upstreamEcscmChangeLogs.each { p ->
			println p.propertyName.toString()
			println p.value.toString()
			writer.append(p.propertyName.toString())
			writer.append(p.value.toString())
			println "Written the same to the file."
		}
		writer.append(
		"""
            ***********************************************************************************************
        """
		)
		logger.info("ChangeListReport is in - " + changeListReport.absolutePath)
		writer.close()
	}

	private void copyArtifactsForUseByEC () {
		new CommanderArtifacts(new CommanderClient()).copyToArtifactsDir(upstream_changelog_file)
	}


	private void publishArtifactLinksToEC() {
		def artifacts = new CommanderArtifacts(new CommanderClient());
		artifacts.publishLinks()
	}

}
