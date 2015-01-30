package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException

import static org.apache.commons.lang.StringUtils.isBlank

class XcodeArchiveConfig implements XcodeConfig {

    private Property<String> version
    private Property<String> shortVersionString

    String getVersion() {
        return version.get()
    }

    void setVersion(Object version) {
        this.version = new Property(version)
    }

    String getShortVersionString() {
        return shortVersionString.get()
    }

    void setShortVersionString(Object shortVersionString) {
        this.shortVersionString = new Property(shortVersionString)
    }

    @Override
    void validate() throws InvalidXcodeConfigurationException {
        def errorMessages = ''

        if (version == null || isBlank(version.get())) {
            errorMessages += "- Please define the ipa `version`.\n"
        }

        if (shortVersionString == null || isBlank(shortVersionString.get())) {
            errorMessages += "- Please define the ipa `shortVersionString`."
        }

        if (!isBlank(errorMessages)) {
            throw new InvalidXcodeConfigurationException(errorMessages)
        }
    }
}
