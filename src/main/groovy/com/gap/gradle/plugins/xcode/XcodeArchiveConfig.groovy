package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException

import static org.apache.commons.lang.StringUtils.isBlank

class XcodeArchiveConfig implements XcodeConfig {

    private Property<String> version
    private Property<String> shortVersionString
    private Property<String> scmRevision

    String getVersion() {
        return version?.get()
    }

    void setVersion(Object version) {
        this.version = new Property(version)
    }

    String getShortVersionString() {
        return shortVersionString?.get()
    }

    void setShortVersionString(Object shortVersionString) {
        this.shortVersionString = new Property(shortVersionString)
    }

    String getScmRevision() {
        scmRevision?.get()
    }

    void setScmRevision(Object scmRevision) {
        this.scmRevision = new Property<String>(scmRevision)
    }

    @Override
    void validate() throws InvalidXcodeConfigurationException {
        def errorMessages = []

        if (isBlank(getVersion())) {
            errorMessages << "- Please define the ipa `version`."
        }

        if (isBlank(getShortVersionString())) {
            errorMessages << "- Please define the ipa `shortVersionString`."
        }

        if (!errorMessages.isEmpty()) {
            throw new InvalidXcodeConfigurationException(errorMessages.join("\n"))
        }
    }
}
