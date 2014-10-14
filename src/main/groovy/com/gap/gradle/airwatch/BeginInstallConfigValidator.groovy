package com.gap.gradle.airwatch

import static org.apache.commons.lang.StringUtils.isBlank

class BeginInstallConfigValidator {
    void validate(BeginInstallConfig config) {
        if (isBlank(config.appName)) {
            throw new RuntimeException("Please configure `appName`.")
        }
        if (isBlank(config.appDescription)) {
            throw new RuntimeException("Please configure `appDescription`.")
        }
        if (isBlank(config.pushMode)) {
            throw new RuntimeException("Please configure `pushMode`.")
        }

        def pushMode = config.pushMode.toLowerCase()
        if (pushMode != "auto" && pushMode != "ondemand") {
            throw new RuntimeException("Invalid `pushMode`, needs to be 'auto' or 'ondemand'.")
        }

        if (isBlank(config.locationGroupId)) {
            throw new RuntimeException("Please configure `locationGroupId` for the environment.")
        }
    }
}