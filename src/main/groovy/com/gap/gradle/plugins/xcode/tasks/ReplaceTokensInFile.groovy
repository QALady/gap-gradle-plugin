package com.gap.gradle.plugins.xcode.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import static org.apache.commons.lang.StringUtils.isBlank

class ReplaceTokensInFile extends DefaultTask {

    String targetFilePath
    Map<String, String> tokensToReplace

    @TaskAction
    def replace() {
        tokensToReplace.each { token, value ->
            if (isBlank(value)) {
                return
            }

            println "Replacing token ${token} with value \"${value}\" in ${targetFilePath}"

            project.ant.replace(file: targetFilePath, token: token, value: value)
        }
    }

}
