package com.gap.gradle.plugins.iossigning

import com.gap.gradle.plugins.airwatch.util.CommandRunner

class PlistBuddy {
    public static final String PLISTBUDDY_TOOL = "/usr/libexec/PlistBuddy"

    private final CommandRunner commandRunner

    def PlistBuddy(CommandRunner commandRunner) {
        this.commandRunner = commandRunner
    }

    public String printEntry(String entryName, File plistFile) {
        commandRunner.run(PLISTBUDDY_TOOL, "-x", "-c", "Print ${entryName}", plistFile.absolutePath)
    }
}
