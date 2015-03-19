package com.gap.gradle.tasks

import com.gap.gradle.utils.Barrier
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static java.util.concurrent.TimeUnit.SECONDS

class StartBackgroundProcessTask extends DefaultTask {

    private static final int MAX_RETRIES = 12
    private static final int RETRY_INTERVAL = 5

    String command
    String directory
    File pidFile

    StartBackgroundProcessTask() {
        directory = '.'
    }

    @TaskAction
    def exec() {
        validateParameters()

        println "Starting background process with command \"${command}\"...\n"

        invokeCommand()

        waitForProcessToFinish()
    }

    private void waitForProcessToFinish() {
        try {
            new Barrier(MAX_RETRIES, RETRY_INTERVAL, SECONDS).executeUntil {
                def pid = getPid(command)

                if (pid.isEmpty()) {
                    println 'Waiting for process to start...'

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

    private static void writePidToFile(String pid, File file) {
        file.append(pid + "\n")
    }

    private static String getPid(String command) {
        ["bash", "-c", "pgrep -f '${command}'"].execute().text.trim()
    }

    private Process invokeCommand() {
        new ProcessBuilder(command.split())
                .redirectErrorStream(true)
                .directory(new File(directory))
                .start()
    }

    private void validateParameters() {
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
