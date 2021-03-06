package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.gradle.api.Action
import org.gradle.internal.reflect.Instantiator

import static org.apache.commons.lang.StringUtils.isBlank

class XcodeTestConfig implements XcodeConfig {
    private XcodeExtension extension
    private Property<String> scheme
    private Property<String> target
    DestinationConfig destination

    XcodeTestConfig(Instantiator instantiator, XcodeExtension extension) {
        this.destination = instantiator.newInstance(DestinationConfig)
        this.extension = extension
    }

    String getScheme() {
        def testScheme = scheme?.get()
        return isBlank(testScheme) ? extension.scheme : testScheme
    }

    void setScheme(Object scheme) {
        this.scheme = new Property(scheme)
    }

    void destination(Action<DestinationConfig> action) {
        action.execute(destination)
    }

    String getTarget() {
        def testTarget = target?.get()
        return isBlank(testTarget) ? extension.scheme : testTarget
    }

    void setTarget(Object target) {
        this.target = new Property(target)
    }

    @Override
    void validate() throws InvalidXcodeConfigurationException {
        def errorMessages = []

        if (isBlank(getScheme())) {
            errorMessages << "Please configure the `xcode.scheme` or the `xcode.test.scheme`."
        }
        if (extension.isTargetRequired() && isBlank(target?.get())) {
            errorMessages << "Please configure the `xcode.test.target` or `xcode.scheme`. See available targets with `xcodebuild -list`."
        }

        try {
            destination.validate()
        }
        catch(InvalidXcodeConfigurationException e) {
            errorMessages << e.message
        }
        
        if(!errorMessages.isEmpty()) {
            throw new InvalidXcodeConfigurationException(errorMessages.join("\n"))
        }
    }
}
