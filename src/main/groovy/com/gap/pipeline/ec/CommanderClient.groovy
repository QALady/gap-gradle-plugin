package com.gap.pipeline.ec

import groovy.json.JsonSlurper

import org.slf4j.LoggerFactory

import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException
import com.gap.pipeline.utils.Environment

class CommanderClient {

	ShellCommand shellCommand
	def environment
	def logger = LoggerFactory.getLogger(CommanderClient)
	private final String PROJECT_NAME_PROPERTY = '/myJob/projectName'
	private final String PROCEDURE_NAME_PROPERTY = '/myJob/liveProcedure'

	CommanderClient(shellCommand = new ShellCommand(), environment = new Environment()) {
		this.shellCommand = shellCommand
		this.environment = environment
	}

	/**
	 * Usage: createProcedure <projectName> <procedureName>
				[--description <description>]
				[--credentialName <credentialName>]
				[--resourceName <resourceName>]
				[--workspaceName <workspaceName>]
				[--jobNameTemplate <jobNameTemplate>]
				[--timeLimit <timeLimit>]
				[--timeLimitUnits <hours|minutes|seconds>]
	 * @param projectName
	 * @param procedureName
	 * @return
	 * commands: '--description', '--resourceName', '--workspaceName'
	 */
	def createProcedure(projectName, procedureName, Map config = [:]) {
		def command = ['ectool', 'createProcedure', projectName.toString(), procedureName.toString()]

		populateCommand(config, command)

		logger.info("createProcedure: " + command.toString())
		return shellCommand.execute(command)
	}

	/**
	 * Usage: createStep <projectName> <procedureName> <stepName>
				[--description <description>]
				[--credentialName <credentialName>]
				[--resourceName <resourceName>]
				[--command <command>]
				[--subprocedure <subprocedure>]
				[--subproject <subproject>]
				[--workingDirectory <workingDirectory>]
				[--timeLimit <timeLimit>]
				[--timeLimitUnits <hours|minutes|seconds>]
				[--postProcessor <postProcessor>]
				[--parallel <0|1|true|false>]
				[--logFileName <logFileName>]
				[--actualParameter <var1>=<val1> [<var2>=<val2> ...]]
				[--exclusive <0|1|true|false>]
				[--exclusiveMode <none|job|step|call>]
				[--releaseExclusive <0|1|true|false>]
				[--releaseMode <none|release|releaseToJob>]
				[--alwaysRun <0|1|true|false>]
				[--shell <shell>]
				[--errorHandling <failProcedure|abortProcedure|abortProcedureNow|abortJob|abortJobNow|ignore>]
				[--condition <condition>]
				[--broadcast <0|1|true|false>]
				[--workspaceName <workspaceName>]
				[--precondition <precondition>]
				[--commandFile <commandFile>]
	 * @param projectName
	 * @param procedureName
	 * @param stepName
	 * @return
	 * commands: '--condition', '--parallel', '--subproject', '--subprocedure', '--actualParameter', '--command'
	 */
	def createStep(projectName, procedureName, stepName, Map config = [:]) {
		def command = ['ectool', 'createStep', projectName.toString(), procedureName.toString(), stepName.toString()]

		populateCommand(config, command)

		logger.info("createStep: " + command.toString())
		return shellCommand.execute(command)
	}

	def getPlugin(pluginName) {
		try {
			String output= shellCommand.execute(['ectool', 'getPlugin', pluginName])
			logger.debug("ectool getPlugin response: " + output)
			def response = new XmlSlurper().parseText(output)
			logger.debug("xml slurped response: " + response)
			return response.plugin
		}
		catch (ShellCommandException e) {
			if (e.message.contains('[NoSuchPlugin]')) {
				logger.debug("Requested plugin does not exist. ${e.message}\n")
			} 
			throw e
		}
	}

	def getCurrentProjectName() {
		getECProperty(PROJECT_NAME_PROPERTY).value
	}

	private def getCurrentProcedureName() {
		getECProperty(PROCEDURE_NAME_PROPERTY).value
	}

	private def getProjectName(jobId) {
		def projectNameProperty = "/jobs[$jobId]/projectName"
		getECProperty(projectNameProperty).value
	}

	private def getProcedureName(jobId) {
		def projectNameProperty = "/jobs[$jobId]/liveProcedure"
		getECProperty(projectNameProperty).value
	}


	def addLink(filename, jobid) {
		def filenameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'))
		setECProperty("/jobs[${jobid}]/report-urls/${filenameWithoutExtension}", "/commander/jobs/${jobid}/default/${filename}")
	}

	def addLinkToUrl(linkName, linkUrl, jobId = getJobId()) {
		setECProperty("/jobs[${jobId}]/report-urls/${linkName}", linkUrl)
	}

	def addLinkToRunProcedureInJob(linkName, procedureName) {
		addLinkToUrl(linkName, getRunProcedureUrl(procedureName), getJobId())
	}

	def getJobId() {
		environment.getValue('COMMANDER_JOBID')
	}

	def getCurrentJobDir() {
		environment.getValue('COMMANDER_WORKSPACE_UNIX')
	}

	def setDefaultParameterValue(fullProcedureName, parameterName, defaultValue) {
		def procedure = parseProcedureName(fullProcedureName)
		shellCommand.execute(['ectool', 'modifyFormalParameter', procedure.projectName, procedure.procedureName, parameterName, '--defaultValue', defaultValue])
	}

	static def getRunProcedureUrl(fullProcedureName) {
		def procedure = parseProcedureName(fullProcedureName)
		URI uri = new URI(
				"https",
				"commander.gapinc.dev",
				"/commander/link/runProcedure/projects/${procedure.projectName}/procedures/${procedure.procedureName}", null);
		uri.toString() + "?s=Projects"
	}

	private static parseProcedureName(String fullProcedureName) {
		def parts = fullProcedureName.split(':')
		if (parts.size() < 2) {
			throw new IllegalArgumentException("The procedure name '${fullProcedureName}' is invalid. It should be of the format '<project name>:<procedure name>'")
		}
		[projectName: parts[0], procedureName: parts[1]]
	}


	public setECProperty(name, value) {
		shellCommand.execute(['ectool', 'setProperty', name.toString(), value.toString()])
	}

	public def getECProperty(key) {
		try {
			return new Property(key, shellCommand.execute(['ectool', 'getProperty', key.toString()]))
		}
		catch (ShellCommandException e) {
			if (e.message.contains('[NoSuchProperty]')) {
				logger.debug("Requested property does not exist. ${e.message}\n")
				return Property.invalidProperty(key)
			} else throw e
		}
	}

	def getCurrentSegmentConfig() {
		new SegmentConfig(getSegmentConfigPropertyValue('configSCMUrl'),
				getSegmentConfigPropertyValue('workingDir'),
				getSegmentConfigPropertyValue('ciDir'),
				getSegmentConfigPropertyValue('gradleFile'),
				getSegmentConfigPropertyValue('scmConfigName'),
				isManualSegment())
	}

	private boolean isManualSegment() {
		def isManualProperty = getSegmentConfigProperty('config/isManual')
		def isManual = isManualProperty.isValid() ? Boolean.valueOf(isManualProperty.value) : false;
		isManual
	}

	private String getSegmentConfigPropertyValue(String property) {
		getSegmentConfigProperty(property).value
	}

	private Property getSegmentConfigProperty(String property) {
		getECProperty('/myJob/watchmen_config/' + property)
	}

	def getUserId() {
		getECProperty("/myJob/launchedByUser").value
	}

	def getUserName() {
		def jobTriggeredByUserId = getECProperty("/myJob/launchedByUser").value
		isJobTriggeredManually(jobTriggeredByUserId) ? getECProperty("/users[$jobTriggeredByUserId]/fullUserName").value : jobTriggeredByUserId
	}

	def getStartTime() {
		getECProperty("/myJob/start").value
	}

	private static def isJobTriggeredManually(userId) {
		!userId.toString().contains(' ')
	}

	def getCurrentSegment() {
		return new Segment(getCurrentProjectName(), getCurrentProcedureName())
	}

	def getSegment(jobId) {
		return new Segment(getProjectName(jobId), getProcedureName(jobId))
	}

	def isRunningInPipeline() {
		getJobId() != null
	}

	def getArtifactoryUserName() {
		getCredential("""/projects/WM Credentials/credentials/WMArtifactory""", """userName""")
	}

	def getArtifactoryPassword() {
		getCredential("""/projects/WM Credentials/credentials/WMArtifactory""", """password""")
	}

	def getCredential(String credentialName, String valueName) {
		if (isRunningInPipeline()) {
			try {
				shellCommand.execute(['ectool', 'getFullCredential', credentialName, '--value', valueName])
			} catch (ShellCommandException se) {
				if (se.getMessage().contains('ectool error [InvalidCredentialName]')) {
					//logger.warn('WARNING: Using dummy credentials - Only WM Gradle:Invoke & WM Exec:Run are approved steps to access artifactory credentials. This will not impact your job unless you are trying to use the Artifactory credentials in this step')
					return 'dummy'
				} else {
					throw se
				}
			}
		} else {
			//logger.warn('Credentials are accesible only in pipline')
		}
	}

	public Property getReportUrlPropertyOfJob(jobId, String key) {
		return getECProperty("/jobs[$jobId]/report-urls/" + key)
	}

	/**
	 * @param pConfig
	 * @return
	 * commands: "--path", "--recurse", "--propertySheetId", "--expand"
	 */
	public def getECProperties(Map pConfig) {
		def slurpedJson
		def command = ['ectool', '--format', 'json', 'getProperties']

		populateCommand(pConfig, command)

		try {
			logger.info("ectool getProperties command: " + command)
			def output = shellCommand.execute(command)
			slurpedJson = new JsonSlurper().parseText(output)
			logger.info("slurped getProperties data: " + slurpedJson)
		}
		catch (ShellCommandException e) {
			if (e.message.contains('[NoSuchPropertySheet]')) {
				logger.debug("Requested property sheet does not exist. ${e.message}\n")
				return Property.invalidProperty(pConfig.toString())
			} else throw e
		}
		return slurpedJson
	}

	def private  populateCommand(Map pConfig, command) {
		pConfig.each { key, value ->
			command.add("--${key.toString().trim()}".toString())
			command.add("'${value.toString().trim()}'".toString())
			logger.info("populateCommand: --"+key.toString().trim().toString())
			logger.info("populateCommand: '"+value.toString().trim().toString()+"'")
		}
	}

	def getBaseUrl() {
		return getECProperty("/server/baseUrl").value
	}

}
