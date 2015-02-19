package com.gap.gradle.tasks

import groovy.xml.MarkupBuilder

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException
import com.gap.pipeline.ec.CommanderArtifacts
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.Property
import com.gap.pipeline.tasks.WatchmenTask

class GenerateAndLinkUpstreamChangelogReportTask extends WatchmenTask {
	def logger = LogFactory.getLog(com.gap.gradle.tasks.GenerateAndLinkUpstreamChangelogReportTask)
	def upstream_changelog_file = "${project.projectDir}/UpStream_ChangeList_Report.html"
	def thisJobId
	Project project
	ShellCommand shellCommand = new ShellCommand()
	CommanderClient commanderClient = new CommanderClient(shellCommand)
	StringWriter changeLogMarkupWriter = new StringWriter()
	MarkupBuilder changeLogMarkupBuilder = new MarkupBuilder(changeLogMarkupWriter)
	StringWriter linkMarkupWriter = new StringWriter()
	MarkupBuilder linkMarkupBuilder = new MarkupBuilder(linkMarkupWriter)

	public GenerateAndLinkUpstreamChangelogReportTask(Project project) {
		super(project);
		this.project = project;
		this.thisJobId = commanderClient.getJobId()
	}

	public def execute() {
		validate()
		try {
			processUpstreamChangeLog()
		} catch (all) {
			logger.info("Unable to generate upstream Change Log report.")
			logger.debug(all.message)
		}
	}

	def processUpstreamChangeLog() {
		def immediateUpStreamJobId = getUpstreamJobId(thisJobId)
		if (immediateUpStreamJobId) {
			File changeListReport = new File(upstream_changelog_file)
			def writer = changeListReport.newWriter()
			addUpstreamChangeLogToMarkup(immediateUpStreamJobId)
			writeChangeLogMarkup(writer)
			writer.close()
			logger.info("ChangeListReport generated in - " + changeListReport.absolutePath)
			copyArtifactsForUseByEC()
			publishArtifactLinksToEC()
			logger.info("Upstream Change Log report linked to EC Job.")
		} else {
			logger.info("No Upstream segment job linked to this segment. No upstream Change Log.")
		}
	}

	def addUpstreamChangeLogToMarkup(givenUpStreamJobId) {
		logger.info("adding ChangeLog of UPSTREAM Job ID: $givenUpStreamJobId")
		buildChangelogMarkup(getECSCMPropertySheetRecords(givenUpStreamJobId))
		//buildChangelogMarkup(commanderClient.getECProperties([path: "/jobs[" + givenUpStreamJobId + "]/ecscm_changeLogs", recurse: 1]))
		buildLinkMarkup(givenUpStreamJobId)
		// check if this upStreamJob has an upstream to it:
		def nextUpstreamJobId = getUpstreamJobId(givenUpStreamJobId)
		if (nextUpstreamJobId) {
			addUpstreamChangeLogToMarkup(nextUpstreamJobId)
		}
	}

	def getUpstreamJobId(givenJobId) {
		// get the Upstream Job report-url property and extract upstream JobID.
		def upStreamJobId
		Property upStreamJobProperty = commanderClient.getReportUrlPropertyOfJob(givenJobId, "Upstream Job")
		if (upStreamJobProperty.isValid()) {
			upStreamJobId = upStreamJobProperty.value.tokenize("/").last()
			return upStreamJobId
		}
		return null
	}

	def getECSCMPropertySheetRecords(upstreamJobId) {
		def prop
		try{
			prop = shellCommand.execute(['ectool', 'getProperties', '--path', "/jobs[" + upstreamJobId + "]/ecscm_changeLogs", '--recurse', '1'])
		}
		catch (ShellCommandException e) {
			if(e.message.contains('[NoSuchProperty]')){
				logger.debug("Requested property does not exist. ${e.message}\n")
				return Property.invalidProperty("ecscm_changeLogs")
			}
			else {
				throw e
			}
		}
		logger.debug("Change Log from Upstream: " + prop)
		def response = new XmlSlurper().parseText(prop)
		return response.propertySheet.property // return all the changelog property records.
	}

	def buildLinkMarkup(givenJobId) {
		linkMarkupBuilder.tr {
			td {
				a(href: "/commander/link/jobDetails/jobs/$givenJobId", commanderClient.getSegment(givenJobId).toString())
			}
		}

	}

	def buildChangelogMarkup(upstreamChangeLogs) {
		upstreamChangeLogs.each { prop ->
			changeLogMarkupBuilder.p {
				table {
					tr {
						td {
							mkp.yield prop.propertyName.toString()
						}
					}
					tr {
						td {
							b {
								mkp.yieldUnescaped(prop.value.toString().replaceAll("\n","<br>"))
							}
						}
					}
				}
			  }
			}
	}
	void writeChangeLogMarkup(writer) {
		def builder = new MarkupBuilder(writer)
		builder.html {
			head {
				title "EC:Upstream ChangeLog Report:"
			}
			body {
				h1"Upstream ChangeLog Report"
				h2 {
					table {
						tr {
							td {
								a(href: "/commander/link/jobDetails/jobs/$thisJobId", "this Job: " + commanderClient.getCurrentSegment().toString())
							}
						}
						mkp.yieldUnescaped(linkMarkupWriter.toString())
					}
				}
				mkp.yieldUnescaped(changeLogMarkupWriter.toString())
			}
		}
	}

	private void copyArtifactsForUseByEC () {
		new CommanderArtifacts(commanderClient).copyToArtifactsDir(upstream_changelog_file)
	}


	private void publishArtifactLinksToEC() {
		def artifacts = new CommanderArtifacts(commanderClient);
		artifacts.publishLinks()
	}

}
