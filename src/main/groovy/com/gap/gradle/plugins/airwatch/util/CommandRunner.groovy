package com.gap.gradle.plugins.airwatch.util

import org.gradle.api.Project

class CommandRunner {
    private final Project project

    def CommandRunner(Project project) {
        this.project = project
    }

    public String run(Object... args) {
        run(new File("."), args)
    }

    public String run(File baseDir, Object... args) {
        def result = new ByteArrayOutputStream().withStream { os ->
            project.exec {
                workingDir baseDir
                commandLine args
                standardOutput = os
            }
            os.toString()
        }

        result
    }
}
