package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException

import static org.apache.commons.lang.StringUtils.isBlank

class DestinationConfig implements XcodeConfig {
    private Property<String> platform
    private Property<String> name
    private Property<String> os

    String getPlatform() {
        return platform.get()
    }

    void setPlatform(Object platform) {
        this.platform = new Property(platform)
    }

    String getName() {
        return name.get()
    }

    void setName(Object name) {
        this.name = new Property(name)
    }

    String getOs() {
        return os.get()
    }

    void setOs(Object os) {
        this.os = new Property(os)
    }

    void validate() {
        def errorMessage = ''

        if (platform == null || isBlank(platform.get())) {
            errorMessage += "Please configure the `platform` in test destination."
        }

        if (name == null || isBlank(name.get())) {
            errorMessage += "Please configure the `name` in test destination."
        }

        if (os == null || isBlank(os.get())) {
            errorMessage += "Please configure the `os` in test destination."
        }

        if (!isBlank(errorMessage)) {
            throw new InvalidXcodeConfigurationException(errorMessage)
        }
    }
}
