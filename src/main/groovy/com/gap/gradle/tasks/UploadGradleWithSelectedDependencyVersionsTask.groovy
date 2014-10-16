package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.SegmentRegistry
import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters


@RequiredParameters([
	@Require(parameter = 'segmentIdentifier', description = 'segmentIdentifier that describes the Project:Procedure to kick off manual segment of. <project name>:<segment name>'),
	@Require(parameter = 'selectedVersions', description = 'string containing the chosen versions of the segment dependencies. Comma Delimited. ex: <group>:<app>:<version>,<group2>:<app2>:<version2>')
	])
class UploadGradleWithSelectedDependencyVersionsTask extends WatchmenTask {
	def logger = LogFactory.getLog(com.gap.gradle.tasks.UploadGradleWithSelectedDependencyVersionsTask)
	Project project
	CommanderClient commanderClient
	SegmentRegistry segmentRegistry

	public UploadGradleWithSelectedDependencyVersionsTask(Project project, commanderClient = new CommanderClient(), segmentRegistry = new SegmentRegistry()) {
		super(project);
		this.project = project
		this.commanderClient = commanderClient
		this.segmentRegistry = segmentRegistry
	}

	def execute() {
		validate()
		logger.info("Passed param segmentIdentifier as = " + project.segmentIdentifier)
		logger.info("Passed param selectedVersions as = " + project.selectedVersions)

		def gradleFileName = "ci/" + segmentRegistry.getSegmentRegistryValue(project.segmentIdentifier, "gradleFile")

		logger.info("Gradle file of the segmentIdentifier is = $gradleFileName")
		logger.info("Segment Gradle file is: " + new File(gradleFileName).getText())

		replaceGradleWithSelectedVersions(gradleFileName, project.selectedVersions.split(","))

		logger.info("Updated segment Gradle file dependencies with selected versions of artifacts given.")
		logger.info("Gradle file changed to: " + new File(gradleFileName).getText())
	}
	
	def replaceGradleWithSelectedVersions(gradleFileName, selectedVersions) {
		File gradleFile = new File(gradleFileName)
		def patterns = [:]
		selectedVersions.each { selectedVersion ->
			// example: each selected version is like com.gap.test:test-app:1.0.123
			def artifact = selectedVersion.split(":")
			def pattern = ~/group\s*:\s*'${artifact[0]}'\s*,\s*name\s*:\s*'${artifact[1]}'\s*,\s*version\s*:\s*'\d+(\.?\d)*'/
			def replacement = "group: \'${artifact[0]}\', name: \'${artifact[1]}\', version: \'${artifact[2]}\'"
			patterns.put(pattern, replacement)
		}
		
	   StringBuffer stringWriter = new StringBuffer()
	   def ls = System.getProperty('line.separator')
	   gradleFile.eachLine{ line ->
		   patterns.each { pattern, replacement ->
			   line = line.replaceAll(pattern, replacement)
		   }
		   stringWriter.append(line).append(ls)
	   }
	   gradleFile.write(stringWriter.toString())
	}
}
