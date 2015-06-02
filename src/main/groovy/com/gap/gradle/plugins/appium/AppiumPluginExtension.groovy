package com.gap.gradle.plugins.appium

import org.gradle.api.Project

class AppiumPluginExtension {

    private List<String> serverFlags
    boolean simulatorMode
    File logFile
    String instrumentsTemplateURI
    String trustStoreFileURI

    AppiumPluginExtension(Project project) {
        this.serverFlags = []
        this.simulatorMode = true
        this.logFile = new File(project.buildDir, "test/logs/appium.log")

        configureDefaultFlags()
    }

    private void configureDefaultFlags() {
        setExtendedServerFlags("--session-override")
        setExtendedServerFlags("--log-no-colors")
        setExtendedServerFlags("--log-timestamp")
    }

    public String appiumServerArguments() {
        def argsList = []
        argsList.addAll(serverFlags)
        argsList.add("&>")
        argsList.add(logFile.absolutePath)

        return argsList.join(' ').trim()
    }

    public void setExtendedServerFlags(String flag) {
        this.serverFlags << flag
    }
}
