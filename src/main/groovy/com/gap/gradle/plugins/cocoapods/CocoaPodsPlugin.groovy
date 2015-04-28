package com.gap.gradle.plugins.cocoapods

import org.gradle.api.Plugin
import org.gradle.api.Project

class CocoaPodsPlugin implements Plugin<Project> {

    private static final String COCOAPODS_REPO_NAME = 'Gap-CocoaPods'

    private Project project
    private PodSpecExtension podspec
    private PodspecValidator podspecValidator = new PodspecValidator()

    @Override
    void apply(Project project) {
        this.project = project
        this.podspec = project.extensions.create('podspec', PodSpecExtension, project)

        def updatePodspec = project.task("updatePodspec", type: UpdatePodspecTask) {
            doFirst {
                podspecValidator.validate(podspec)

                def originalFile = new File(project.rootDir, "${extension.podName}.podspec")

                podspecFile = originalFile
                tokens = [POD_NAME: podspec.name, POD_VERSION: podspec.version]
            }
        }

        project.task("pushPodspec", type: UploadPodspecTask, dependsOn: "updatePodspec") {
            doFirst {
                podspecFile = updatePodspec.output
                podRepo = COCOAPODS_REPO_NAME
            }
        }
    }
}
