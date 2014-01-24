package com.gap.gradle.chef

import com.gap.gradle.utils.ShellCommand
import groovy.json.JsonSlurper

class CookbookUtil {

    def metadataFrom(cookbookDir = ".") {
        new ShellCommand().execute("knife cookbook metadata from file ${"${cookbookDir}/metadata.rb"}")
        new JsonSlurper().parseText(new File("${cookbookDir}/metadata.json").text)
    }
}
