package com.gap.pipeline.ec

import com.gap.pipeline.utils.Environment
import com.gap.pipeline.utils.ShellCommand

class CommanderClient {

    def shellCommand
    def environment

    CommanderClient(shellCommand = new ShellCommand(), environment = new Environment()) {
        this.shellCommand = shellCommand
        this.environment = environment
    }

    def addLink(filename, jobid){
        def filenameWithoutExtension = filename.substring(0, filename.lastIndexOf('.'))
        setECProperty("/jobs[${jobid}]/report-urls/${filenameWithoutExtension}", "/commander/jobs/${jobid}/default/${filename}")
    }

    def addLinkToUrl(linkName, linkUrl, jobId){
        setECProperty("/jobs[${jobId}]/report-urls/${linkName}", linkUrl)
    }

    def addLinkToRunProcedureInJob(linkName, procedureName){
        addLinkToUrl(linkName, getRunProcedureUrl(procedureName), getJobId())
    }

    def getJobId() {
        environment.getValue('COMMANDER_JOBID')
    }

    def runProcedure(String fullProcedureName, params = []){
       def procedure = parseProcedureName(fullProcedureName)
       def command = buildShellCommand(procedure.projectName,procedure.procedureName,params)
       shellCommand.execute(command)
    }

    private parseProcedureName(String fullProcedureName) {
        def parts = fullProcedureName.split(':')
        if (parts.size() < 2) {
            throw new IllegalArgumentException("The procedure name '${fullProcedureName}' is invalid. It should be of the format '<project name>:<procedure name>'")
        }
        [projectName: parts[0], procedureName: parts[1]]
    }

    def getCurrentJobDir (){
        environment.getValue('COMMANDER_WORKSPACE_UNIX')
    }

    def setDefaultParameterValue(fullProcedureName, parameterName, defaultValue) {
        def procedure = parseProcedureName(fullProcedureName)
        shellCommand.execute(['ectool', 'modifyFormalParameter', procedure.projectName, procedure.procedureName, parameterName, '--defaultValue', defaultValue])
    }

    def getRunProcedureUrl(fullProcedureName) {
        def procedure = parseProcedureName(fullProcedureName)
        URI uri = new URI(
            "https",
            "commander.phx.gapinc.dev",
            "/commander/link/runProcedure/projects/${procedure.projectName}/procedures/${procedure.procedureName}",
            null);
        uri.toString()+"?s=Projects"
    }

    private setECProperty(name, value) {
        shellCommand.execute(['ectool', 'setProperty', name.toString(), value.toString()])
    }

    private def buildShellCommand(projectName, procedureName, params) {
        def command =  ["ectool", "runProcedure",projectName, "--procedureName", procedureName]
        if (params) {
            command << "--actualParameter"
            params.each { key, value ->
                def parameter = "${key}=${value}".toString() //tostring makes junit asserts happy
                command << parameter
            }
        }
        command
    }

	private def getECProperty(property) {
		shellCommand.execute(['ectool', 'getProperty', property.toString()])
	}

	def getUserId(){
		def jobTriggeredByUserId = getECProperty("/myJob/launchedByUser")
	}

	def getUserName(){
		def jobTriggeredByUserId = getECProperty("/myJob/launchedByUser")
		def jobTriggeredByUserName = getECProperty("/users[$jobTriggeredByUserId]/fullUserName")
	}

	def getStartTime(){
		def jobTriggeredTime = getECProperty("/myJob/start/")
	}
}