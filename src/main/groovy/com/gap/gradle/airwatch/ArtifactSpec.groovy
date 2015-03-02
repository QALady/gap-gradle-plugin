package com.gap.gradle.airwatch

class ArtifactSpec {
    String groupId
    String name
    String classifier
    String type

    boolean hasAtLeastOneSpecDefined() {
        [groupId, name, classifier, type].any { it != null }
    }
}
