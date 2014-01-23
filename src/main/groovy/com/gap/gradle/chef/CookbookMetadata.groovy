package com.gap.gradle.chef

import com.gap.gradle.utils.ShellCommand

class CookbookMetadata {
    def cookbookName
    def version

    def loadFrom(cookbookDir = ".") {

        generateMetadataJson(cookbookDir)

    }

    def generateMetadataJson(def cookbookDir)
    {
        def metadataFile = "$cookbookDir/metadata.rb"
        new ShellCommand().execute("knife cookbook metadata from file ${metadataFile}")
    }
}
