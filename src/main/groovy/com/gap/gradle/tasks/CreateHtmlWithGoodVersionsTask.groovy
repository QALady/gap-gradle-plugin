package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.ec.CommanderClient
import com.gap.gradle.utils.ShellCommand

class CreateHtmlWithGoodVersionsTask extends WatchmenTask {
	def logger = LogFactory.getLog(com.gap.gradle.tasks.CreateHTMLWithGoodVersionsTask)
	ShellCommand shellCommand = new ShellCommand()
	CommanderClient commanderClient = new CommanderClient(shellCommand)

	def execute() {
		try {
				buildDependenciesHtml()
		}catch (all) {
				logger.info("Unable to create HTML with good versions")
				
		}
	}

	def buildDependenciesHtml() {
		
	}

}
