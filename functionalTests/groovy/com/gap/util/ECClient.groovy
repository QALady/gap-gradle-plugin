package com.gap.util

import org.apache.commons.logging.LogFactory

class ECClient {
    def shellCommand
    def logger = LogFactory.getLog(ShellCommand)

    ECClient() {
        this.shellCommand = new ShellCommand()
    }

    def runProcedureSync(fullProcedureName, params = []){
        def procedure = parseProcedureName(fullProcedureName)
        def command = buildShellCommand(procedure.projectName,procedure.procedureName,params)
        def jobId = shellCommand.execute(command)
        Util.executeWithRetry(30, 0.5, {getJobStatus(jobId).status == 'completed'})
        jobId
    }

    def getJobStatus(def jobId) {
        new XmlSlurper().parseText(shellCommand.execute("ectool getJobStatus ${jobId}"))
    }

    def getECProperty(property, jobId = null){
        def propertyName = jobId == null ? "/myJob/${property}" : "/jobs[${jobId}]/${property}"
        shellCommand.execute(['ectool', 'getProperty', propertyName])
    }

    def setProperty(property, value, jobId = null){
        def propertyName = jobId == null ? "/myJob/${property}" : "/jobs[${jobId}]/${property}"
        shellCommand.execute(['ectool', 'getProperty', propertyName])
    }

    def getJobId() {
        System.getenv()['COMMANDER_JOBID']
    }

    def isRunningInPipeline(){
        getJobId() != null
    }

    private parseProcedureName(fullProcedureName) {
        def parts = fullProcedureName.split(':')
        if (parts.size() < 2) {
            throw new IllegalArgumentException("The procedure name '${fullProcedureName}' is invalid. It should be of the format '<project name>:<procedure name>'")
        }
        [projectName: parts[0], procedureName: parts[1]]
    }

    private def buildShellCommand(projectName, procedureName, params) {
        def command =  ["ectool", "runProcedure", projectName, "--procedureName", procedureName]
        if (params) {
            command << "--actualParameter"
            params.each { key, value ->
                def parameter = "${key}=${value}".toString() //tostring makes junit asserts happy
                command << parameter
            }
        }
        command
    }
}
