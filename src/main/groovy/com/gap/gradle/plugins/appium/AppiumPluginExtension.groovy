package com.gap.gradle.plugins.appium

import org.gradle.api.Project

class AppiumPluginExtension {

    private List<String> defaultServerFlags
    private List<String> extendedServerFlags
    boolean simulatorMode = true
    File logFile

    AppiumPluginExtension(Project project) {
        this.logFile = new File(project.buildDir, "test/logs/appium.log")
        this.defaultServerFlags = ["--session-override", "--log-no-colors", "--log-timestamp", "&>", logFile.absolutePath]
        this.extendedServerFlags = []
    }

    public String appiumServerArguments() {
        return (defaultServerFlags + extendedServerFlags).join(' ').trim()
    }

    public void setExtendedServerFlags(String extendedServerFlags) {
        this.extendedServerFlags << extendedServerFlags
    }
}
