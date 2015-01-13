package com.gap.gradle.airwatch.util

import org.gradle.api.Project

class CaptureExecOutput {
    private final Project project

    def CaptureExecOutput(Project project) {
        this.project = project
    }

    public String outputOf(Object... args) {
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
