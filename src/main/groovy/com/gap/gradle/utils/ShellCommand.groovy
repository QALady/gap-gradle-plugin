package com.gap.gradle.utils

import org.apache.commons.logging.LogFactory

class ShellCommand {

    def logger = LogFactory.getLog(ShellCommand)

    def execute(command, path) {
        logger.info("Executing command ${command} ...")
        def proc
        if(path == null){
            proc = command.execute()
        }
        else{
            proc = command.execute(null, path)
        }
        def exitCode = proc.waitFor()
        def output = proc.in.text
        logger.info("${output}")
        if (exitCode == 0) {
            logger.info("Command completed successfully")
            output
        } else {
            def errorText = proc.err.text
            logger.error("Command failed with exit code ${exitCode}")
            logger.error(errorText)
            throw new ShellCommandException("Command execution failed! Exit code ${exitCode}: ${errorText}");
        }
    }
}
