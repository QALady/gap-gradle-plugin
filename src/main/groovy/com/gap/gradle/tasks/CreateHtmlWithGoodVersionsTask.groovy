package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.ec.CommanderClient
import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.SegmentRegistry

class CreateHtmlWithGoodVersionsTask extends WatchmenTask {
	def logger = LogFactory.getLog(com.gap.gradle.tasks.CreateHTMLWithGoodVersionsTask)
	ShellCommand shellCommand = new ShellCommand()
	CommanderClient commanderClient = new CommanderClient(shellCommand)
	SegmentRegistry segmentRegistry = new SegmentRegistry()

	def execute() {
		try {
				buildDependenciesHtml()
		}catch (all) {
				logger.info("Unable to create HTML with good versions")
				//logger.debug(all.message)
		}
	}

	def buildDependenciesHtml() {
		def ivyDependencies = [] 
		ivyDependencies = commanderClient.getECProperty("/myJob/ivyDependencies")
		//def dependenciesHtml = "<table border=\"0\">"
		ivyDependencies.each { dependency -> 
			def segmentId = segmentRegistry.getSegmentThatProducesIdentifier(dependency)
			def versions =[] 
			versions = segmentRegistry.getSuccessfulSegmentVersions(segmentId) 
		}
	}
}
