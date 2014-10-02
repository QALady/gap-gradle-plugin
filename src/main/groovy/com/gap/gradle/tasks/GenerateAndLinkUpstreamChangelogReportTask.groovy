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
	Project project
	ShellCommand shellCommand = new ShellCommand()
	CommanderClient commanderClient = new CommanderClient(shellCommand)
	def logger = LogFactory.getLog(com.gap.gradle.tasks.GenerateAndLinkUpstreamChangelogReportTask)
	def upstream_changelog_file = "${project.projectDir}/UpStream_ChangeList_Report.html"
	def thisJobId
	def upstreamJobId
	def upstreamJobIds = []

	public GenerateAndLinkUpstreamChangelogReportTask(Project project) {
		super(project);
		this.project = project;
		this.thisJobId = commanderClient.getJobId()
	}

	public def execute() {
		validate()
		upstreamJobId = getUpstreamJobId()
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
		buildChangelogMarkup(writer, upstreamEcscmChangeLogs)
		writer.close()
		logger.info("ChangeListReport generated in - " + changeListReport.absolutePath)
	}

	void buildChangelogMarkup(def writer, upstreamChangeLogs) {
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
								a(href: "/commander/link/jobDetails/jobs/$upstreamJobId", commanderClient.getSegment(upstreamJobId).toString())
							}
						}
						tr {
							td {
								a(href: "/commander/link/jobDetails/jobs/$thisJobId", commanderClient.getCurrentSegment().toString())
							}
						}
					}
				}
				upstreamChangeLogs.each { prop ->
				p {
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
		}
	}
	
	private void copyArtifactsForUseByEC () {
		new CommanderArtifacts(new CommanderClient()).copyToArtifactsDir(upstream_changelog_file)
	}


	private void publishArtifactLinksToEC() {
		def artifacts = new CommanderArtifacts(new CommanderClient());
		artifacts.publishLinks()
	}

}
