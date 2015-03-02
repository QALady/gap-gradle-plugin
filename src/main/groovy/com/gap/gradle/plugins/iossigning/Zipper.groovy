package com.gap.gradle.plugins.iossigning

import org.gradle.api.Project

class Zipper {
    private final Project project

    def Zipper(Project project) {
        this.project = project
    }

    def unzip(File zipFile, File destinationDir) {
        project.ant.unzip(src: zipFile.absolutePath, dest: destinationDir.absolutePath)
    }

    def zip(File baseDir, File destinationZip) {
        project.ant.zip(destfile: destinationZip, baseDir: baseDir)
    }
}
