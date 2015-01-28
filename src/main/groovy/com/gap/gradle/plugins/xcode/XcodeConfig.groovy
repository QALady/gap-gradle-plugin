package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException

interface XcodeConfig {
    void validate() throws InvalidXcodeConfigurationException
}