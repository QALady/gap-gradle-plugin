package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.gradle.api.Action
import org.gradle.internal.reflect.Instantiator

import static org.apache.commons.lang.StringUtils.isBlank

class XcodeTestConfig implements XcodeConfig {
    private Property<String> scheme
    DestinationConfig destination

    XcodeTestConfig(Instantiator instantiator) {
        this.destination = instantiator.newInstance(DestinationConfig)
    }

    String getScheme() {
        return scheme.get()
    }

    void setScheme(Object scheme) {
        this.scheme = new Property(scheme)
    }

    void destination(Action<DestinationConfig> action) {
        action.execute(destination)
    }

    @Override
    void validate() throws InvalidXcodeConfigurationException {
        if (scheme == null || isBlank(scheme.get())) {
            throw new InvalidXcodeConfigurationException("Please configure the `scheme`.")
        }

        destination.validate()
    }
}
