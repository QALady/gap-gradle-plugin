package com.gap.gradle.chef

import com.gap.gradle.utils.ShellCommand
import groovy.json.JsonSlurper
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory

class CookbookUtil {

    private Log log = LogFactory.getLog(CookbookUtil)

    def metadataFrom(cookbookDir = ".") {
        new ShellCommand().execute("knife cookbook metadata from file ${"${cookbookDir}/metadata.rb"}")
        new JsonSlurper().parseText(new File("${cookbookDir}/metadata.json").text)
    }

    def doesCookbookExist(cookbookMetadata) {
        try {
            def output = new ShellCommand().execute("knife cookbook show ${cookbookMetadata.name}")
            log.info("Output: ${output} - ${cookbookMetadata.version}")
            return output.contains(" ${cookbookMetadata.version} ")
        }
        catch(Exception) {
            return false
        }
    }
}
