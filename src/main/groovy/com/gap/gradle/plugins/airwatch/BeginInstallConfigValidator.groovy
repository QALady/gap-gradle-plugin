package com.gap.gradle.plugins.airwatch

import static org.apache.commons.lang.StringUtils.isBlank

class BeginInstallConfigValidator {
    void validate(BeginInstallConfig config) {
        if (isBlank(config.appName)) {
            throw new ValidationException("Please configure `appName`.")
        }
        if (isBlank(config.appDescription)) {
            throw new ValidationException("Please configure `appDescription`.")
        }
        if (isBlank(config.pushMode)) {
            throw new ValidationException("Please configure `pushMode`.")
        }

        def pushMode = config.pushMode.toLowerCase()
        if (pushMode != "auto" && pushMode != "ondemand") {
            throw new ValidationException("Invalid `pushMode`, needs to be 'auto' or 'ondemand'.")
        }

        if (isBlank(config.locationGroupId)) {
            throw new ValidationException("Please configure `locationGroupId` for the environment.")
        }
    }
}
