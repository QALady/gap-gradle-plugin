package com.gap.gradle.plugins.cocoapods

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class UploadPodspecTask extends DefaultTask {

    private final CocoaPodsCommandRunner cocoapods

    File podspecFile
    String podRepo

    UploadPodspecTask() {
        cocoapods = new CocoaPodsCommandRunner(project)
    }

    @TaskAction
    void upload() {
        if (!podspecFile) {
            throw new GradleException("Please define `podspecFile`.")
        }

        if (!podRepo) {
            throw new GradleException("Please define `podRepo`.")
        }

        println "Listing existing pod repos..."
        cocoapods.listRepos()

        println "Pushing podspec $podspecFile to repository $podRepo..."
        cocoapods.pushPodspec(podspecFile, podRepo)
    }
}
