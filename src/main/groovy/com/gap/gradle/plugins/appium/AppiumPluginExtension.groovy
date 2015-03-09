package com.gap.gradle.plugins.appium
import org.gradle.api.Project

class AppiumPluginExtension {
  
    private String extendedServerFlags
    private final Project project

    boolean simulatorMode = true
    File logFile

    AppiumPluginExtension(Project project) {
        this.project = project
        this.logFile = new File(project.buildDir, "test/logs/appium.log")
    }

    public String appiumServerArguments() { 
        if (getExtendedServerFlags() != null) {
            return getDefaultServerFlags() + ' ' + getExtendedServerFlags()
        } else {
            return getDefaultServerFlags()
        }
    }

    String getDefaultServerFlags() {
        return " --log-no-colors --log-timestamp --log ${logFile.absolutePath}"
    }

    String getExtendedServerFlags() {
        return extendedServerFlags
    }

    void setExtendedServerFlags(String extendedServerFlags) {
        this.extendedServerFlags = extendedServerFlags
    }
}
