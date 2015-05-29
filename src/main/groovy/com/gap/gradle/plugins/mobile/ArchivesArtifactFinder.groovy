package com.gap.gradle.plugins.mobile

import com.gap.gradle.plugins.airwatch.ArtifactFinder
import com.gap.gradle.plugins.airwatch.ArtifactSpec
import org.gradle.api.Project
import org.gradle.api.artifacts.ResolvedArtifact

class ArchivesArtifactFinder {
    private final Project project

    ArchivesArtifactFinder(Project project) {
        this.project = project
    }

    public ResolvedArtifact find(ArtifactSpec spec) {
        def artifactFinder = new ArtifactFinder(spec)
        return project.configurations['archives'].resolvedConfiguration.resolvedArtifacts.find {
            def matchResult = artifactFinder.matches(it)
            project.logger.info "Artifact ${it} from archives ${matchResult ? 'matches':'does not match'} artifact spec."
            matchResult
        }
    }
}
