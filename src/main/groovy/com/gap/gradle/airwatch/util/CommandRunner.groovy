package com.gap.gradle.airwatch.util

import org.gradle.api.Project

class CommandRunner {
    private final Project project

    def CommandRunner(Project project) {
        this.project = project
    }

    public String run(Object... args) {
        def result = new ByteArrayOutputStream().withStream { os ->
            project.exec {
                commandLine args
                standardOutput = os
            }
            os.toString()
        }

        result
    }
}
