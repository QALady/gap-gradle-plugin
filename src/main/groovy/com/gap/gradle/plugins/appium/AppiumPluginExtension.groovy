package com.gap.gradle.plugins.appium
import org.gradle.api.Project

class AppiumPluginExtension {
  
    private String defaultServerFlags
    private String extendedServerFlags
    boolean simulatorMode = true
    private Project project

    AppiumPluginExtension(Project project) {
        this.project = project
    }
    
    public String appiumServerArguments() { 
        if (getExtendedServerFlags() != null) {
            return getDefaultServerFlags() + ' ' + getExtendedServerFlags()
        } else {
            return getDefaultServerFlags()
        }
    }

    String getDefaultServerFlags() {
        return " --log-no-colors --log-timestamp --log ${project.buildDir}/test/logs/appium.log"
    }

    String getExtendedServerFlags() {
        return extendedServerFlags
    }

    void setExtendedServerFlags(String extendedServerFlags) {
        this.extendedServerFlags = extendedServerFlags
    }

}
