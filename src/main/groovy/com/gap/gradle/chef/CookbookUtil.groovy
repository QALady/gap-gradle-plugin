package com.gap.gradle.chef

import com.gap.gradle.utils.ShellCommand
import groovy.json.JsonSlurper

class CookbookUtil {

    def metadataFrom(cookbookDir = ".") {
        new ShellCommand().execute("knife cookbook metadata from file ${"${cookbookDir}/metadata.rb"}")
        new JsonSlurper().parseText(new File("${cookbookDir}/metadata.json").text)
    }

    def doesCookbookExist(cookbookMetadata) {
        try {
            def output = new ShellCommand().execute("knife cookbook show ${cookbookMetadata.name}")
            return output.contains(" ${cookbookMetadata.version} ")
        }
        catch(Exception)
        {
            return false
        }
    }
}
