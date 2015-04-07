package com.gap.gradle.plugins.cocoapods

import org.gradle.api.Project

class CocoaPodsCommandRunner {

    private static final String POD_BINARY = "pod"

    private Project project

    public CocoaPodsCommandRunner(Project project) {
        this.project = project
    }

    public String listRepos() {
        project.exec {
            commandLine POD_BINARY, "repo", "list"
        }
    }

    public void pushPodspec(File podspecFile, String repoName) {
        project.exec {
            commandLine POD_BINARY, "repo", "push", repoName, podspecFile.absolutePath
        }
    }
}
