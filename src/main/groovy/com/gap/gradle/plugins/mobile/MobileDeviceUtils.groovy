package com.gap.gradle.plugins.mobile

class MobileDeviceUtils {
    public static final String LIST_ATTACHED_DEVICE = "system_profiler SPUSBDataType | sed -n -e '/iPod/,/Serial/p' | grep 'Serial Number:' | cut -d ':' -f 2"

    private final CommandRunner commandRunner

    public MobileDeviceUtils(CommandRunner commandRunner) {
        this.commandRunner = commandRunner
    }

    public String listAttachedDevices() {
        commandRunner.run("bash", "-c", LIST_ATTACHED_DEVICE).trim()
    }
}
