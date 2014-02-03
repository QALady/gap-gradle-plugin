package com.gap.gradle.plugins.cookbook

import groovy.transform.PackageScope

class ChefConfig {

    def cookbookName
    def environment = "tdev"
    def cookbookDir = "."

    /**
     * Metadata should not be user-configurable: it is populated by task {@link GenerateCookbookMetadataTask}.
     */
    @PackageScope
    def metadata
}
