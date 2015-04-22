package com.gap.pipeline.tasks

import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import com.gap.pipeline.ec.CommanderClient
import com.gap.gradle.utils.ShellCommand
import groovy.io.FileType

@RequiredParameters([
    @Require(parameter = "artifactLocation", description = "Location of the artifacts which needs to be ")
])

class LinkArtifactsTask extends com.gap.pipeline.tasks.WatchmenTask {

	private project
	CommanderClient commanderClient
	def artifactLocation
	def commanderArtifactLocation
	def log = LogFactory.getLog(com.gap.pipeline.tasks.LinkArtifactsTask)

	LinkArtifactsTask(project) {
		super(project)
		this.project = project
		this.commanderClient = new CommanderClient()
		this.artifactLocation = project.artifactLocation
		this.commanderArtifactLocation = "{commanderClient.currentJobDir}/artifacts"
	}

	private void copyArtifacts(artifactLocation) {
		ShellCommand shellCommandExecutor =  new ShellCommand()
		shellCommandExecutor.execute("cp -R ${this.artifactLocation} ${this.commanderArtifactLocation}")

	}
	private void createHtmlIndex() {
		def dir =  new File("${this.commanderArtifactLocation}")
		dir.eachFileRecurse(FileType.DIRECTORIES) { subDir -> 
			ShellCommand shellCmdExecutor = new ShellCommand("${subDir}")
			println "subDir.... ${subDir}"
			shellCmdExecutor.execute("tree --dirsfirst -CFo dir.html -H . -L 1 -I dir.html -T PWD")
		   } 
  
	}
	private void linkArtifacts() {
		commanderClient.addLink("${this.commanderArtifactLocation}/dir.html", commanderClient.getJobId())

	}
	def execute(){
		log.info("Executing task linkArtifacts...")
		copyArtifacts()
		createHtmlIndex()
		linkArtifacts()
	}


}
