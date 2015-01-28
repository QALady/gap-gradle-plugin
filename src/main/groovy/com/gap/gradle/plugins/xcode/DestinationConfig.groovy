package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException

import static org.apache.commons.lang.StringUtils.isBlank

class DestinationConfig implements XcodeConfig {
    String platform
    String name
    String os

    void validate() {
        if (isBlank(platform) || isBlank(name) || isBlank(os)) {
            throw new InvalidXcodeConfigurationException("Please configure the test destination (`platform`, `name` and `os`).")
        }
    }
}
