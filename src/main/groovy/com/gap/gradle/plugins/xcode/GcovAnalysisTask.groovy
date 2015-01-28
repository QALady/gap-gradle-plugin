package com.gap.gradle.plugins.xcode

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction

class GcovAnalysisTask extends DefaultTask {

    @Input
    String fileNamePattern

    @OutputDirectory
    File reportsDir

    @TaskAction
    def run() {
        reportsDir.mkdirs()

        def filesToAnalyse = project.fileTree(project.buildDir).include(fileNamePattern)

        filesToAnalyse.each { file ->
            project.exec {
                executable = 'gcov'
                args = [file, '--branch-counts', '--branch-probabilities', '--object-directory', file.parent]
                workingDir = reportsDir
            }
        }
    }

}
