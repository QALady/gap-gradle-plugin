package com.gap.gradle.plugins.appium
import org.gradle.api.Project

class AppiumPluginExtension {

    private String defaultServerFlags
    private final Project project
    private String extendedServerFlags
    boolean simulatorMode = true
    File logFile

    AppiumPluginExtension(Project project) {
        this.project = project
        this.logFile = new File(project.buildDir, "test/logs/appium.log")
        this.defaultServerFlags = " --log-no-colors --log-timestamp --log ${logFile.absolutePath}"
    }

    public String appiumServerArguments() {
        if (getExtendedServerFlags() != null) {
            return getDefaultServerFlags() + ' ' + getExtendedServerFlags()
        } else {
            return getDefaultServerFlags()
        }
    }

    String getDefaultServerFlags() {
        return defaultServerFlags
    }

    String getExtendedServerFlags() {
        return extendedServerFlags
    }

    void setExtendedServerFlags(String extendedServerFlags) {
        if(this.extendedServerFlags == null){
            this.extendedServerFlags = extendedServerFlags
        } else {
            this.extendedServerFlags << extendedServerFlags
        }
    }
}
