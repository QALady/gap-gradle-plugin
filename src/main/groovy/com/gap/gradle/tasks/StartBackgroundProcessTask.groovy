package com.gap.gradle.tasks

import com.gap.gradle.utils.Barrier
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static java.util.concurrent.TimeUnit.SECONDS

import org.apache.commons.logging.LogFactory

class StartBackgroundProcessTask extends DefaultTask {

    private static final logger = LogFactory.getLog(StartBackgroundProcessTask)
    private static final int MAX_RETRIES = 12
    private static final int RETRY_INTERVAL = 5

    String command
    String directory
    File pidFile
    Barrier barrier = new Barrier(MAX_RETRIES, RETRY_INTERVAL, SECONDS)

    StartBackgroundProcessTask() {
        directory = '.'
    }

    @TaskAction
    def exec() {
        validateParameters()

        logger.info("Starting background process with command \"${command}\"...\n")

        executeCommand(command)

        waitForProcessToFinish()
    }

    def waitForProcessToFinish() {
        try {
            barrier.executeUntil {
                def pid = getPidForCommand(command)

                if (pid.isEmpty()) {
                    logger.info('Waiting for process to start...')

                    return false
                } else {
                    project.logger.debug("Writing pid $pid to $pidFile.absolutePath")

                    writePidToFile(pid, pidFile)

                    return true
                }
            }
        } catch (Barrier.MaxNumberOfTriesReached e) {
            throw new GradleException("Process Timeout: Command \"${command}\" did not start in ${MAX_RETRIES * RETRY_INTERVAL} seconds", e);
        }
    }

    def writePidToFile(String pid, File file) {
        file.append(pid + "\n")
    }

    def getPidForCommand(String cmd) {
        executeCommand("pgrep -f '$cmd'").text.trim()
    }

    def executeCommand(String cmd) {
        ["bash", "-c", cmd].execute()
    }

    def validateParameters() {
        def errorMessages = []

        if (!command) {
            errorMessages << "- Please define `command` to execute"
        }

        if (!pidFile || !pidFile.exists()) {
            errorMessages << "- Please define a `pidFile` to read PIDs from"
        }

        if (!errorMessages.isEmpty()) {
            throw new GradleException(errorMessages.join("\n"))
        }
    }
}
