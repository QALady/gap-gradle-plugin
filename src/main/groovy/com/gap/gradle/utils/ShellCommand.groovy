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

    def execute(command, environment = null) {
        logger.info("Executing command ${command} ...")
        def proc = command.execute(environment, workingDir)
        def exitCode = proc.waitFor()
        def output = proc.in.text
        logger.debug("${output}")
        if (exitCode == 0) {
            logger.info("Command completed successfully")
            output
        } else {
            def errorText = proc.err.text
            logger.info("throwing ShellCommandException...")
            throw new ShellCommandException("Command execution failed! Exit code ${exitCode}: ${errorText}");
        }
    }
}
