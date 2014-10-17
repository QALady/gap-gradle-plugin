package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.SegmentRegistry

class CreateHtmlWithGoodVersionsTask extends WatchmenTask{
	def logger = LogFactory.getLog(com.gap.gradle.tasks.CreateHtmlWithGoodVersionsTask)
	CommanderClient commanderClient
	SegmentRegistry segmentRegistry

	CreateHtmlWithGoodVersionsTask(Project project, commanderClient = new CommanderClient(), segmentRegistry = new SegmentRegistry()) {
		super(project)
		this.project = project
		this.commanderClient = commanderClient
		this.segmentRegistry = segmentRegistry
	}

	def execute() {
		validate()
		try {
	
				buildDependenciesHtml()
				
		}catch (all) {
				logger.info("Unable to create HTML with good versions")
				//logger.debug(all.message)
		}
	}

	def getIvyDependencies() {

		ivyDependencies[] = commanderClient.getECProperty("/myJob/ivyDependencies").split("\n")
	}
	
	def buildDependenciesHtml() {

		ivyDependencies[] = getIvyDependencies()
		def dependenciesHtml = "<table border=\"0\">"

		ivyDependencies.each { dependency ->
			def segmentId = segmentRegistry.getSegmentThatProducesIdentifier(dependency)
			def versions = segmentRegistry.getSuccessfulSegmentVersions(segmentId) 
			dependenciesHtml += createTableRow(dependency, segmentId, versions )
		}
		dependenciesHtml += "</table><br>"
		commanderClient.setECProperty("/myJob/dependenciesHtml", dependenciesHtml)
	}

	def createTableRow(String dependency, String segmentId, versions) {
        def resolvedDependencies = "";
        //def dynamicData = commanderClient.getProperty("/myJob/dynamicData")->findvalue('//value')->value();
        def rowHtml = "<tr>\n<td>\n<label for=\"$segmentId\"> $segmentId </label>\n</td>\n"
        rowHtml += "<td><select id=\"$segmentId\" name=\"dependency\" onchange=\"showFields(this)\">"

				
		my latestVersion = MAX(versions);

		rowHtml += "<option value=\"$dependency:$latestVersion\">$latestVersion (latest)</option>\n"
        resolvedDependencies = segmentRegistry.getResolvedDependencies(segmentId, latestVersion)
        dynamicData += "<h4>Dependencies for the segment: $segmentId</h4><div id=$dependency:$latestVersion style=\"display: block;\"><pre> $resolvedDependencies </pre></div>\n"

  		versions.each { version ->
					rowHtml += "<option value=\"$dependency:$version\">$version</option>\n"
					resolvedDependencies = segmentRegistry.getResolvedDependencies(segmentId, version)
					dynamicData += "<div id=$dependency:$version style=\"display: none;\"><pre> $resolvedDependencies </pre></div>\n"
		}

        rowHtml += "</select>\n</td>\n</tr>"
        
        commanderClient.setECProperty("/myJob/dynamicData", dynamicData);
 				return rowHtml;
			
		}
}
