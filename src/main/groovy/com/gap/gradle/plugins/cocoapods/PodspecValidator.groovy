package com.gap.gradle.plugins.cocoapods

import org.gradle.api.GradleException

import static org.apache.commons.lang.StringUtils.isBlank

class PodspecValidator {

    def validate(Podspec podspec) {
        if (isBlank(podspec.name)) {
            throw new GradleException("Please specify the `podspec.name`.")
        }

        if (isBlank(podspec.version)) {
            throw new GradleException("Please specify the `podspec.version` or `project.version`.")
        }

        if (isBlank(podspec.sourceLocation)) {
            throw new GradleException("Please specify the `podspec.sourceLocation`.")
        }
    }
}
