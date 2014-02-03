package com.gap.gradle.plugins.cookbook

class ChefConfig {

    def cookbookName
    def environment = "tdev"
    def cookbookDir = "."

    /**
     * Metadata should not be user-configurable: it is populated by task {@link GenerateCookbookMetadataTask}.
     */
    def metadata
}
