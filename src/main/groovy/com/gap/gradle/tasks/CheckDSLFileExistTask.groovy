package com.gap.gradle.tasks

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.tasks.WatchmenTask
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

class CheckDSLFileExistTask extends WatchmenTask {

	def logger = LogFactory.getLog(CheckDSLFileExistTask)

	public enum SegmentType
	{
		app_segment, component_segment, normal_segment
	};

	SegmentType currentSegmentType

	String workingDir
	String segmentName
	CommanderClient commanderClient
	boolean isDSL
	boolean isSegmentProperties
	String segmentConfigFile
	final static String JOB_SHEET = '/myJob/watchmen_config'
	Project project
	String projectDir

	CheckDSLFileExistTask(Project project, commanderClient = new CommanderClient()) {
		super(project)
		this.project = project
		this.commanderClient = commanderClient
		initializeProperties()
		identifySegmentType()
	}

	def execute() {
		boolean existsPropertiesFile = checkIfPropertiesFileExists()

		logger.info("Properties file $segmentConfigFile exists --> $existsPropertiesFile")

		if (existsPropertiesFile) {
			isSegmentProperties = true
			commanderClient. setECProperty(JOB_SHEET + "/segmentConfigFile", segmentConfigFile)
		} else {
			setupGradleSegmentConfigFile()
			isDSL = true
			commanderClient.setECProperty(JOB_SHEET + "/segmentConfigFile", segmentConfigFile)
		}
		commanderClient.setECProperty(JOB_SHEET + "/isDSL", isDSL)
		commanderClient.setECProperty(JOB_SHEET + "/isSegmentProperties", isSegmentProperties)

	}

	boolean checkIfPropertiesFileExists() {

		def propertiesFileName = segmentName + ".properties"

		propertiesFileName = projectDir + '/' + propertiesFileName

		segmentConfigFile = propertiesFileName

		logger.info("<properties_file> : $segmentConfigFile")

		File propertiesFile = new File(propertiesFileName)

		logger.info("Absolute properties file can be : " + propertiesFile.getAbsoluteFile())

		return propertiesFile.exists()
	}

	void initializeProperties() {
		segmentName = commanderClient.getECProperty('/myJob/watchmen_config/segmentName').getValue()
		workingDir = commanderClient.getECProperty('/myJob/watchmen_config/workingDir').getValue()
		projectDir = project.getProjectDir().toString()
	}

	def identifySegmentType() {
		logger.info("segmentName is $segmentName")
		try {
			currentSegmentType = segmentName.replaceAll('-', '_') as SegmentType
		}
		catch (ignored) {
			logger.info("segmentName cast to normal_segment")
			currentSegmentType = SegmentType.normal_segment
		}
	}

	def setupGradleSegmentConfigFile() {
		logger.info("currentSegmentType is $currentSegmentType")
		if (currentSegmentType == SegmentType.normal_segment) {
			segmentConfigFile = "ci/${segmentName}.gradle"
			logger.info("<normal_segment> : $segmentConfigFile")
		} else {
			segmentConfigFile = "build.gradle"
			logger.info("<app_segment, component_segment> : $segmentConfigFile")
		}
	}
}
