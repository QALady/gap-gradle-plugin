package com.gap.gradle.plugins.cocoapods

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class CocoaPodsPlugin implements Plugin<Project> {

    private static final String COCOAPODS_REPO_NAME = 'Gap-CocoaPods'

    private Project project
    private PodSpecExtension extension

    @Override
    void apply(Project project) {
        this.project = project
        this.extension = project.extensions.create('podspec', PodSpecExtension, project)

        def updatePodspec = project.task("updatePodspec", type: UpdatePodspecTask) {
            doFirst {
                PodspecValidator.validate(extension)

                def originalFile = new File(project.rootDir, "${extension.podName}.podspec")

                podspecFile = originalFile
                tokens = [POD_NAME: extension.podName, POD_VERSION: extension.podVersion, ARTIFACT_URL: artifactUrl]
            }
        }

        project.task("pushPodspec", type: UploadPodspecTask, dependsOn: "updatePodspec") {
            doFirst {
                podspecFile = updatePodspec.output
                podRepo = COCOAPODS_REPO_NAME
            }
        }
    }

    private String getArtifactUrl() {
        def artifactoryRepo = project.uploadArchives.repositories.find { it.url }
        def group = project.group.replaceAll('\\.', '/')

        if (!artifactoryRepo) {
            throw new GradleException('No Artifactory repository found for uploadArchives task')
        }

        "${artifactoryRepo.url}/${group}/${extension.podVersion}/${extension.podName}.framework.zip".toString()
    }
}
