package com.gap.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class StopProcessByPortTask extends DefaultTask {

    List<String> ports

    @TaskAction
    def exec() {
        ports.each { port ->
            def cmd = "lsof -Fp -i :$port"
            def process = cmd.execute()

            process.inputStream.eachLine { line ->
                def killCommand = "kill -9 ${line.substring(1)}".execute()
                killCommand.waitFor()
            }
        }
    }
}
