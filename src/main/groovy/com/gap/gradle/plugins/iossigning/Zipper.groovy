package com.gap.gradle.plugins.iossigning

import com.gap.gradle.plugins.mobile.CommandRunner

class Zipper {
    public static final String ZIP_TOOL = "/usr/bin/zip"
    public static final String UNZIP_TOOL = "/usr/bin/unzip"

    private final CommandRunner commandRunner

    def Zipper(CommandRunner commandRunner) {
        this.commandRunner = commandRunner
    }

    def unzip(File zipFile, File destinationDir) {
        commandRunner.run(UNZIP_TOOL, "-o", zipFile.absolutePath, "-d", destinationDir.absolutePath)
    }

    def zip(File baseDir, File destinationZip) {
        commandRunner.run(baseDir, ZIP_TOOL, destinationZip.absolutePath, "-r", ".")
    }
}
