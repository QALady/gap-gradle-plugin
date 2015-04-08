package com.gap.gradle.plugins.cocoapods

import org.gradle.api.GradleException

import static org.apache.commons.lang.StringUtils.isBlank

class PodspecValidator {

    def static validate(Podspec spec) {
        if (isBlank(spec.podName)) {
            throw new GradleException("Please specify a `podName`.")
        }

        if (isBlank(spec.podVersion)) {
            throw new GradleException("Please specify the `project.version`.")
        }
    }
}
