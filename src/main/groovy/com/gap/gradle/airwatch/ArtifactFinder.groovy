package com.gap.gradle.airwatch

import org.gradle.api.artifacts.ResolvedArtifact

class ArtifactFinder {
    private final ArtifactSpec artifactSpec

    def ArtifactFinder(ArtifactSpec artifactSpec) {
        this.artifactSpec = artifactSpec
    }

    boolean matches(ResolvedArtifact artifact) {
        matchIfSpecDefined(artifactSpec.name, artifact.name) \
         && matchIfSpecDefined(artifactSpec.groupId, artifact.moduleVersion.id.group) \
         && matchIfSpecDefined(artifactSpec.classifier, artifact.classifier) \
         && matchIfSpecDefined(artifactSpec.type, artifact.type)
    }

    private boolean matchIfSpecDefined(String specValue, String artifactValue) {
        if (specValue) {
            return specValue == artifactValue
        }
        true
    }
}
