package com.gap.gradle.plugins.appium

class AppiumPluginExtension{
    String defaultServerFlags = ' --log-no-colors --log-timestamp --log /var/log/appium.log'
    String serverFlags
    boolean simulatorMode = true

    public String appiumServerArguments(){
    //serverFlags = {project.appiumConfig.serverFlags}
     if (serverFlags != null) {
         return defaultServerFlags + ' ' + serverFlags
     }
     else {
         return defaultServerFlags
     }

    }
}
