package com.gap.gradle.tasks

import com.gap.gradle.utils.GradleOutput
import com.gap.pipeline.tasks.WatchmenTask
import org.apache.commons.io.FilenameUtils
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

class GradlelizeFileTask extends WatchmenTask {

	private String propertyFileName
	def final logger = LogFactory.getLog(GradlelizeFileTask)
	List<String> lines
	String gradlelizedData
	File propertiesFile
	Project project
	File gradlelizedFile

	GradlelizeFileTask(Project project, String propertyFileName = new String()) {
		super(project)
		this.project = project
		this.propertyFileName = propertyFileName
	}

	def execute() {
		readPropertiesFile()
		parseToGradle()
		writeToGradleFile()
	}

	def readPropertiesFile() {
		lines = new ArrayList<>(0)
		propertiesFile = new File(propertyFileName)
		if (propertiesFile.exists()) {
			propertiesFile.eachLine {
				if (it.startsWith("#")) {
					//todo  comments not necessary at this stage
//					lines.add(it.replace("#", '/* ') + " */")
//					logger.info it.replace("#", '/* ') + " */"
				} else if (!it.trim().isEmpty()) {
					lines.add(it)
					logger.info it
				}
			}
		}
	}

	def parseToGradle() {
		def tree = { [:].withDefault { owner.call() } }
		def segment = tree()

		lines.each {
			line ->
				logger.info "line -> " + line
				String[] tokens = line.toString().split("=", 2)
				logger.info "tokens $tokens ${tokens.size()}"
				String[] tokenKeys = tokens[0].split('\\.')
				if (tokenKeys[0].equals('finally')) {
					tokenKeys[0] = '_finally'
				}
				switch (tokenKeys.size()) {
					case 2:
						segment."${tokenKeys[0]}"."${tokenKeys[1]}" = tokens[1]
						break;
					case 3:
						segment."${tokenKeys[0]}"."${tokenKeys[1]}"."${tokenKeys[2]}" = tokens[1]
						break;
					case 4:
						segment."${tokenKeys[0]}"."${tokenKeys[1]}"."${tokenKeys[2]}"."${tokenKeys[3]}".value = tokens[1]
						break;
				}
		}
		gradlelizedData = "segment " + GradleOutput.prettyPrint(GradleOutput.toJson(segment))
	}

	def writeToGradleFile() {
		String gradleFileBaseName = FilenameUtils.getBaseName(propertiesFile.getAbsoluteFile().toString())
		gradlelizedFile = new File(project.getProjectDir(), "${gradleFileBaseName}.gradle")
		gradlelizedFile.write(gradlelizedData)
		logger.info "File should be : " + gradlelizedFile.getAbsolutePath()
	}
}
