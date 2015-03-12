package com.gap.gradle.plugins.mobile

import com.gap.gradle.plugins.airwatch.util.CommandRunner

/**
 * This class defines wrapper methods around the utilities provided by libimobiledevice
 */
class MobileDeviceUtils {
    public static final String IDEVICE_ID_TOOL = '/usr/local/bin/idevice_id'

    private final CommandRunner commandRunner

    public MobileDeviceUtils(CommandRunner commandRunner) {
        this.commandRunner = commandRunner
    }

    public String listAttachedDevices() {
        commandRunner.run(IDEVICE_ID_TOOL, '--list')
    }
}
