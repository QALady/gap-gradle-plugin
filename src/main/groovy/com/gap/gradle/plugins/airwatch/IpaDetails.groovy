package com.gap.gradle.plugins.airwatch

import com.gap.gradle.plugins.iossigning.PlistBuddy
import com.gap.gradle.plugins.mobile.CommandRunner
import org.gradle.api.Project

class IpaDetails {

    private final Project project
    private final CommandRunner commandRunner

    final String bundleIdentifier
    final String bundleVersion

    def IpaDetails(Project project, File ipaFile) {
        this.project = project
        this.commandRunner = new CommandRunner(project)

        File infopList = project.zipTree(ipaFile).matching {
            include "Payload/*.app/Info.plist"
        }.getSingleFile()

        bundleIdentifier = readPlistKeyValue(infopList, "CFBundleIdentifier")
        bundleVersion = readPlistKeyValue(infopList, "CFBundleVersion")
    }

    private String readPlistKeyValue(File plist, String key) {
        def result = new PlistBuddy(commandRunner).printEntry(":${key}", plist)

        project.logger.info "Value from plist for key \"$key\" is \"$result\""

        result
    }
}
