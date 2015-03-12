package com.gap.gradle.tasks

import com.gap.gradle.plugins.airwatch.util.Barrier
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

import static java.util.concurrent.TimeUnit.SECONDS

class SpawnBackgroundProcessTask extends DefaultTask {

    public static final int MAX_RETRIES = 12
    public static final int RETRY_INTERVAL = 5

    String command
    String directory

    SpawnBackgroundProcessTask() {
        directory = '.'
    }

    @TaskAction
    def exec() {
        if (!(command)) {
            throw new GradleException("Please define `command` to execute")
        }

        invokeCommand()

        waitForProcessToFinish()
    }

    private void waitForProcessToFinish() {
        try {
            new Barrier(MAX_RETRIES, RETRY_INTERVAL, SECONDS).executeUntil {
                def command = "ps -ef | grep '${command}' | grep -v grep | wc -l"
                def number_of_processes = ["bash", "-c", command].execute().text.toInteger()

                (number_of_processes == 1)
            }
        } catch (Barrier.MaxNumberOfTriesReached e) {
            throw new GradleException("Process Timeout: Command \"${command}\" did not start in ${MAX_RETRIES * RETRY_INTERVAL} seconds", e);
        }
    }

    private Process invokeCommand() {
        new ProcessBuilder(command.split())
                .redirectErrorStream(true)
                .directory(new File(directory))
                .start()
    }
}
