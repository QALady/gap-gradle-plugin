package com.gap.gradle.utils

import org.apache.commons.logging.LogFactory

class ShellCommand {
    def logger = LogFactory.getLog(ShellCommand)
    void execute(command){
        logger.info ("Executing command ${command} ...")
        def proc = command.execute()
        def exitCode = proc.waitFor()
        logger.info("${proc.in.text}")
        if (exitCode == 0){
            logger.info("Command completed successfully")
        } else {
            logger.error("Command failed with exit code ${exitCode}")
            logger.warn("${proc.err.text}") //temporary hack as EC fails build... should be an error but some times it is expected
            throw new Exception("Command execution failed!!")
        }

    }
}
