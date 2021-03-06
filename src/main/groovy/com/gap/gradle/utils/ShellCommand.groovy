package com.gap.gradle.utils

import org.slf4j.LoggerFactory

class ShellCommand {

    def logger = LoggerFactory.getLogger(ShellCommand)
    def workingDir

    def ShellCommand() {
        this.workingDir = new File('.')
    }

    def ShellCommand(workingDir) {
        this.workingDir = workingDir instanceof File ? workingDir : new File(workingDir)
    }

    def execute(command, environment = null, logOutputToDebug = true) {
        logger.debug("Executing command ${command} ...")
        def proc = command.execute(environment, workingDir)
        def exitCode = proc.waitFor()
        def output = proc.in.text
        if(logOutputToDebug) {
            logger.debug("${output}")
        }
        if (exitCode == 0) {
            logger.debug("Command completed successfully")
            output
        } else {
            def errorText = proc.err.text
            logger.debug("throwing ShellCommandException...")
            throw new ShellCommandException("Command execution failed! Exit code ${exitCode}: ${errorText}");
        }
    }

    def executeNoLog(command, environment = null) {
        execute(command, environment, false)
    }

    def executeReadEachLine(command, environment = null, logOutputToDebug = true) {
        logger.debug("Executing command ${command} ...")
        def proc = command.execute(environment, workingDir)
        StringBuffer output = new  StringBuffer()
        proc.in.eachLine {line -> output.append(line)}
        def exitCode = proc.waitFor()
        if(logOutputToDebug) {
            logger.debug("${output.toString()}")
        }
        if (exitCode == 0) {
            logger.debug("Command completed successfully")
            output.toString()
        } else {
            def errorText = proc.err.text
            logger.debug("throwing ShellCommandException...")
            throw new ShellCommandException("Command execution failed! Exit code ${exitCode}: ${errorText}");
        }
    }
}
