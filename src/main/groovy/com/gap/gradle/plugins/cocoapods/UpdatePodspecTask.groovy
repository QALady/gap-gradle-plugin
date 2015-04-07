package com.gap.gradle.plugins.cocoapods

import org.apache.tools.ant.filters.ReplaceTokens
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

class UpdatePodspecTask extends DefaultTask {

    private File output

    File podspecFile
    Map tokens

    @TaskAction
    void update() {
        if (!podspecFile) {
            throw new GradleException("Please define `podspecFile`.")
        }

        project.copy {
            from podspecFile.parentFile
            into project.buildDir

            include podspecFile.name

            filter(ReplaceTokens, tokens: tokens)
        }

        tokens.each { key, value ->
            println "Replaced token $key with value \"$value\""
        }

        output = new File(project.buildDir, podspecFile.name)
    }

    File getOutput() {
        return output
    }
}
