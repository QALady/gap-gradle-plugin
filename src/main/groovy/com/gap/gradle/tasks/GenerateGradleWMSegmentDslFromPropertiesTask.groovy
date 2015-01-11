package com.gap.gradle.tasks

import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import groovy.json.JsonLexer
import groovy.json.JsonOutput
import groovy.json.JsonToken
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import java.text.SimpleDateFormat

import static groovy.json.JsonTokenType.*

@RequiredParameters([
		@Require(parameter = 'segmentPropertiesFile', description = 'relative or absolute path of the <segment>.properties file of the pipeline <segment> ci folder.')
])
class GenerateGradleWMSegmentDslFromPropertiesTask extends WatchmenTask {

	private String propertyFileName
	def final logger = LogFactory.getLog(GenerateGradleWMSegmentDslFromPropertiesTask)
	List<String> lines
	String gradlelizedData
	File propertiesFile
	Project project
	File gradlelizedFile

	GenerateGradleWMSegmentDslFromPropertiesTask(Project project) {
		super(project)
		this.project = project
		this.propertyFileName = project.segmentPropertiesFile
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
				String[] tokenKeys = tokens[0].replaceAll('-','_').split('\\.')
				if (tokenKeys[0].equals('finally')) {
					tokenKeys[0] = '_finally'
				}
				switch (tokenKeys.size()) {
					case 2:
						segment."${quoteIfSpacesOrNumber(tokenKeys[0])}"."${quoteIfSpacesOrNumber(tokenKeys[1])}" = formatQuotes(tokens[1])
						break;
					case 3:
						segment."${quoteIfSpacesOrNumber(tokenKeys[0])}"."${quoteIfSpacesOrNumber(tokenKeys[1])}"."${quoteIfSpacesOrNumber(tokenKeys[2])}" = formatQuotes(tokens[1])
						break;
					case 4:
						segment."${quoteIfSpacesOrNumber(tokenKeys[0])}"."${quoteIfSpacesOrNumber(tokenKeys[1])}"."${quoteIfSpacesOrNumber(tokenKeys[2])}"."${quoteIfSpacesOrNumber(tokenKeys[3])}".value =  formatQuotes(tokens[1])
						break;
				}
		}
		gradlelizedData = "segment " + GradleOutput.prettyPrint(GradleOutput.toJson(segment))
	}

	static def quoteIfSpacesOrNumber(String tokenKey) {
		if(tokenKey.contains(' ') || tokenKey.matches("^[0-9].*"))
		{
			return "'"+tokenKey+"'"
		}
		return tokenKey
	}

	private static String formatQuotes(String token) {
		return "'" + token.replaceAll("'", '"') + "'"
	}

	def writeToGradleFile() {
		String gradleFileBaseName = getBaseName(propertiesFile.getAbsoluteFile().toString())
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddssSS")
		String timeStamp = simpleDateFormat.format(new Date())
		gradlelizedFile = new File(project.getProjectDir(), "${gradleFileBaseName}-${timeStamp}.gradle")
		gradlelizedFile.write(gradlelizedData)
		println ("File location is : " + gradlelizedFile.getAbsolutePath())
	}

	private static String getBaseName(String absoluteFile) {
		String[] fileTokens = absoluteFile.split("/")
		String name = fileTokens.last()
		int lastIndexOfDot = name.lastIndexOf(".")
		return name.substring(0, lastIndexOfDot)
	}
}

private class GradleOutput extends JsonOutput {

	static String prettyPrint(String jsonPayload) {
		int indent = 0
		def output = new StringBuilder()
		def lexer = new JsonLexer(new StringReader(jsonPayload))

		while (lexer.hasNext()) {
			JsonToken token = lexer.next()
			if (token.type == OPEN_CURLY) {
				indent += 4
				output.append('{\n')
				output.append(' ' * indent)
			} else if (token.type == CLOSE_CURLY) {
				indent -= 4
				output.append('\n')
				output.append(' ' * indent)
				output.append('}')
			} else if (token.type == OPEN_BRACKET) {
				indent += 4
				output.append('[\n')
				output.append(' ' * indent)
			} else if (token.type == CLOSE_BRACKET) {
				indent -= 4
				output.append('\n')
				output.append(' ' * indent)
				output.append(']')
			} else if (token.type == COMMA) {
				output.append('\n')
				output.append(' ' * indent)
			} else if (token.type == COLON) {
				output.append(' ')
			} else if (token.type == STRING) {
				// Cannot use a range (1..-2) here as it will reverse for a string of
				// length 2 (i.e. textStr=/""/ ) and will not strip the leading/trailing
				// quotes (just reverses them).
				String textStr = token.text
				String textWithoutQuotes = textStr.substring(1, textStr.size() - 1)
				output.append((textWithoutQuotes))
			} else {
				output.append(token.text)
			}
		}

		return output.toString()
	}
}

