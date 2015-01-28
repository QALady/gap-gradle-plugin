package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.gradle.api.Action
import org.gradle.internal.reflect.Instantiator

import static org.apache.commons.lang.StringUtils.isBlank

class XcodeTestConfig implements XcodeConfig {
    String scheme
    DestinationConfig destination

    XcodeTestConfig(Instantiator instantiator) {
        this.destination = instantiator.newInstance(DestinationConfig)
    }

    void destination(Action<DestinationConfig> action) {
        action.execute(destination)
    }

    @Override
    void validate() throws InvalidXcodeConfigurationException {
        if (isBlank(scheme)) {
            throw new InvalidXcodeConfigurationException("Please configure the `scheme`.")
        }

        destination.validate()
    }
}
