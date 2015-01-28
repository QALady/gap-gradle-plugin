package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException

import static org.apache.commons.lang.StringUtils.isBlank

class XcodeArchiveConfig implements XcodeConfig {

    String version
    String shortVersionString

    @Override
    void validate() throws InvalidXcodeConfigurationException {
        if (isBlank(version) || isBlank(shortVersionString)) {
            throw new InvalidXcodeConfigurationException("Please set the ipa `version` and `shortVersionString`.")
        }
    }
}
