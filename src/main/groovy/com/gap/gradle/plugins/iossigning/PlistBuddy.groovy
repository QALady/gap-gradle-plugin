package com.gap.gradle.plugins.iossigning

import com.gap.gradle.plugins.mobile.CommandRunner

class PlistBuddy {
    public static final String PLISTBUDDY_TOOL = "/usr/libexec/PlistBuddy"

    private final CommandRunner commandRunner

    def PlistBuddy(CommandRunner commandRunner) {
        this.commandRunner = commandRunner
    }

    public String printEntry(String entryName, File plistFile, boolean outputAsXml = false) {
        def arguments = [PLISTBUDDY_TOOL, '-c', "Print ${entryName}", plistFile.absolutePath]

        if (outputAsXml) arguments.add('-x')

        commandRunner.run(arguments.toArray())
    }
}
