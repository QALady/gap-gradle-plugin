package com.gap.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class StopProcessByPidTask extends DefaultTask {

    File pidFile

    @TaskAction
    def exec() {
        if (noProcessesToStop()) {
            println 'Nothing to stop'
            return
        }

        project.logger.debug("Reading PIDs from $pidFile")

        pidFile.eachLine { pid ->
            println "Stopping process with PID $pid and its sub-processes..."

            def children = "pgrep -P $pid".execute()
            children.inputStream.eachLine { stopProcess(it) }

            stopProcess(pid)
        }

        pidFile.write('')
    }

    private static int stopProcess(String pid) {
        "kill -9 $pid".execute().waitFor()
    }

    private boolean noProcessesToStop() {
        !pidFile.exists() || pidFile.length() == 0
    }
}
