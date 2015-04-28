package com.gap.gradle.plugins.cocoapods

import org.gradle.api.GradleException
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

                podspecFile = getOriginalPodspec()
                tokens = [POD_NAME: podspec.name, POD_VERSION: podspec.version, POD_SOURCE_LOCATION: podspec.sourceLocation]
            }
        }

        project.task("pushPodspec", type: UploadPodspecTask, dependsOn: "updatePodspec") {
            doFirst {
                podspecFile = updatePodspec.output
                podRepo = COCOAPODS_REPO_NAME
            }
        }
    }

    private File getOriginalPodspec() {
        def fileTree = project.fileTree(dir: project.rootDir).matching {
            include "**/${podspec.name}.podspec"
            exclude project.buildDir.name
        }

        if (fileTree.isEmpty()) {
            throw new GradleException("Unable to find ${podspec.name}.podspec. Please check `podspec.name.`")
        }

        def filesFound = fileTree.files.collect { "- ${it.absolutePath}" }

        if (filesFound.size() > 1) {
            throw new GradleException("Ambigous *.podspec files found:\n${filesFound.join("\n")}")
        }

        return fileTree.getSingleFile()
    }
}
