package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException

import static org.apache.commons.lang.StringUtils.isBlank

class XcodeBuildConfig implements XcodeConfig {
    private Property<String> target
    private Property<String> sdk
    private Property<SigningIdentity> signingIdentity

    String getTarget() {
        target.get()
    }

    void setTarget(Object target) {
        this.target = new Property(target)
    }

    String getSdk() {
        sdk.get()
    }

    void setSdk(Object sdk) {
        this.sdk = new Property(sdk)
    }

    SigningIdentity getSigningIdentity() {
        signingIdentity.get()
    }

    void setSigningIdentity(Object signingIdentity) {
        this.signingIdentity = new Property(signingIdentity)
    }

    @Override
    void validate() throws InvalidXcodeConfigurationException {
        def errorMessages = ''

        if (target == null || isBlank(target.get())) {
            errorMessages += "- Please configure the build `target`. See available targets with `xcodebuild -list`.\n"
        }

        if (sdk == null || isBlank(target.get())) {
            errorMessages += "- Please configure with which SDK the target will be built, " +
                    "e.g. `iphoneos` or `iphonesimulator`. See all available SDKs with `xcodebuild -showsdks`.\n"
        }

        if (signingIdentity == null) {
            errorMessages += "- Please choose which signing identity will be used to sign the app. " +
                    "e.g. `development` or `distribution`.\n"
        }

        if (!isBlank(errorMessages)) {
            throw new InvalidXcodeConfigurationException(errorMessages)
        }
    }
}