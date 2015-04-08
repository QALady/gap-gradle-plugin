package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException

import static org.apache.commons.lang.StringUtils.isBlank

class XcodeBuildConfig implements XcodeConfig {
    private XcodeExtension extension
    private Property<String> productName
    private Property<String> target
    private Property<String> sdk
    private Property<SigningIdentity> signingIdentity
    private Property<String> configuration = new Property('Release')

    XcodeBuildConfig(XcodeExtension extension) {
        this.extension = extension
    }

    String getProductName() {
        def productNameValue = productName?.get()
        return isBlank(productNameValue) ? getTarget() : productNameValue
    }

    void setProductName(Object productName) {
        this.productName = new Property(productName)
    }

    String getTarget() {
        def targetValue = target?.get()
        return isBlank(targetValue) ? extension.scheme : targetValue
    }

    void setTarget(Object target) {
        this.target = new Property(target)
    }
   
    void setConfiguration(Object configuration) {
        this.configuration = new Property(configuration)
    }
    
    String getConfiguration() {
        return configuration.get()
    }

    String getSdk() {
        return sdk?.get()
    }

    void setSdk(Object sdk) {
        this.sdk = new Property(sdk)
    }

    SigningIdentity getSigningIdentity() {
        return signingIdentity?.get()
    }

    void setSigningIdentity(Object signingIdentity) {
        this.signingIdentity = new Property(signingIdentity)
    }

    @Override
    void validate() throws InvalidXcodeConfigurationException {
        def errorMessages = []

        if (extension.isTargetRequired() && isBlank(target?.get())) {
            errorMessages << "- Please configure the build `target`, or the `xcode.workspace` and `xcode.scheme`. See available targets with `xcodebuild -list`."
        }

        if (isBlank(getSdk())) {
            errorMessages << "- Please configure with which SDK the target will be built, " +
                    "e.g. `iphoneos` or `iphonesimulator`. See all available SDKs with `xcodebuild -showsdks`."
        }


        if (!errorMessages.isEmpty()) {
            throw new InvalidXcodeConfigurationException(errorMessages.join("\n"))
        }
    }
}
