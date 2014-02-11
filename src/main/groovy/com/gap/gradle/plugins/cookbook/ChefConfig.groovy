package com.gap.gradle.plugins.cookbook

class ChefConfig {

    def cookbookName
    def environment = "tdev"
    def cookbookDir = "."

    /**
     * Feature flag for ITCI-920
     */
    def requirePinnedDependencies = false

    /**
     * Feature flag for ITCI-943
     */
    def requireTransitiveDependencies = false

    /**
     * Metadata should not be user-configurable: it is populated by task {@link GenerateCookbookMetadataTask}.
     */
    def metadata
}
