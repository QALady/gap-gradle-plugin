package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class StopProcessByPidTask extends DefaultTask {

    private static final logger = LogFactory.getLog(StopProcessByPidTask)
    File pidFile

    @TaskAction
    def exec() {
        if (noProcessesToStop()) {
            logger.info('Nothing to stop')
            return
        }

        project.logger.debug("Reading PIDs from $pidFile")

        pidFile.eachLine { pid ->
            logger.info("Stopping process with PID $pid and its sub-processes...")

            def children = "pgrep -P $pid".execute()
            children.inputStream.eachLine { stopProcess(it) }

            stopProcess(pid)
        }

        pidFile.write('')
    }

    def stopProcess(String pid) {
        "kill -9 $pid".execute().waitFor()
    }

    def noProcessesToStop() {
        !pidFile.exists() || pidFile.length() == 0
    }
}
