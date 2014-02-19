package com.gap.pipeline.utils

import org.apache.commons.logging.LogFactory

class ShellCommand {

    def logger = LogFactory.getLog(com.gap.pipeline.utils.ShellCommand)

    def execute(command) {
        logger.info("Executing command ${command} ...")
        def proc = command.execute()
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
            throw new com.gap.pipeline.utils.ShellCommandException("Command execution failed! Exit code ${exitCode}: ${errorText}");
        }
    }
}
