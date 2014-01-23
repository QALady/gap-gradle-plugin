package com.gap.gradle.utils

import org.apache.commons.logging.LogFactory

class Process {
    def logger = LogFactory.getLog(Process)
    void execute(command){
        logger.info ("Executing command ${command} ...")
        def proc = command.execute()
        def exitCode = proc.waitFor()
        logger.info("${proc.in.text}")
        if (exitCode == 0){
            logger.info("Command completed successfully")
        } else {
            logger.error("Command failed with exit code ${exitCode}")
            logger.error("${proc.err.text}")
            throw new Exception("Command execution failed!!")
        }

    }
}
