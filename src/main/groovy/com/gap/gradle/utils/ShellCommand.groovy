package com.gap.gradle.utils

import org.apache.commons.logging.LogFactory

class ShellCommand {
    def logger = LogFactory.getLog(ShellCommand)
    def execute(command){
        logger.info ("Executing command ${command} ...")
        def proc = command.execute()
        def exitCode = proc.waitFor()
        def output = proc.in.text
        logger.info("${output}")
        if (exitCode == 0){
            logger.info("Command completed successfully")
            output
        } else {
            logger.error("Command failed with exit code ${exitCode}")
            logger.error("${proc.err.text}")
            throw new Exception("Command execution failed!!")
        }

    }
}
