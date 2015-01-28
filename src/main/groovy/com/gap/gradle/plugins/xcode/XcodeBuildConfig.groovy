package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException

import static org.apache.commons.lang.StringUtils.isBlank

class XcodeBuildConfig implements XcodeConfig {
    private Object targetObj
    private Object sdkObj
    private Object signingIdentityObj

    String getTarget() {
        if (targetObj instanceof Closure) {
            return targetObj.call()
        }
        targetObj
    }

    void setTarget(Object target) {
        this.targetObj = target
    }

    String getSdk() {
        if (sdkObj instanceof Closure) {
            return sdkObj.call()
        }
        sdkObj
    }

    void setSdk(Object sdk) {
        this.sdkObj = sdk
    }

    Object getSigningIdentity() {
        if (signingIdentityObj instanceof Closure) {
            return signingIdentityObj.call()
        }
        signingIdentityObj
    }

    void setSigningIdentity(Object signingIdentity) {
        this.signingIdentityObj = signingIdentity
    }

    @Override
    void validate() throws InvalidXcodeConfigurationException {
        def errorMessages = ''

        if (isBlank(target)) {
            errorMessages += "- Please configure the build `target`. See available targets with `xcodebuild -list`." +
                    "\n"
        }

        if (isBlank(sdk)) {
            errorMessages += "- Please configure with which SDK the target will be built, " +
                    "e.g. `iphoneos` or `iphonesimulator`. " +
                    "See all available SDKs with `xcodebuild -showsdks`." +
                    "\n"
        }

        if (signingIdentity == null) {
            errorMessages += "- Please choose which signing identity will be used to sign the app. " +
                    "e.g. `development` or `distribution`." +
                    "\n"
        }

        if (!isBlank(errorMessages)) {
            throw new InvalidXcodeConfigurationException(errorMessages)
        }
    }
}
