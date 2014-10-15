package com.gap.gradle.airwatch

import org.junit.Test

import static helpers.ResolvedArtifactFactory.resolvedArtifact
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

public class ArtifactFinderTest {

    @Test
    public void shouldReturnTrueIfSpecMatchesResolvedArtifact() throws Exception {
        ArtifactSpec artifactSpec = new ArtifactSpec();
        artifactSpec.groupId = 'com.example'
        artifactSpec.name = 'fooLib';
        artifactSpec.classifier = 'arm7'
        artifactSpec.type = 'zip'

        ArtifactFinder finder = new ArtifactFinder(artifactSpec);

        def matches = finder.matches(resolvedArtifact(name: 'fooLib', groupId: 'com.example', classifier: 'arm7', type: 'zip'))

        assertTrue("should return true because spec matches resolved artifact", matches)
    }

    @Test
    public void shouldReturnFalseIfAnyAttributeFromResolvedArtifactIsDifferentFromSpec() throws Exception {
        ArtifactSpec artifactSpec = new ArtifactSpec();
        artifactSpec.groupId = 'com.example'
        artifactSpec.name = 'barLib';
        artifactSpec.classifier = 'arm7'
        artifactSpec.type = 'zip'

        ArtifactFinder finder = new ArtifactFinder(artifactSpec);

        def matches = finder.matches(resolvedArtifact(name: 'fooLib', groupId: 'com.example', classifier: 'arm7', type: 'zip'))

        assertFalse("should return false", matches)
    }

    @Test
    public void shouldOnlyCompareAttributesSpecifiedInSpec() throws Exception {
        ArtifactSpec artifactSpec = new ArtifactSpec();
        artifactSpec.name = 'barLib';
        artifactSpec.classifier = 'arm7'

        ArtifactFinder finder = new ArtifactFinder(artifactSpec);

        def matches = finder.matches(resolvedArtifact(name: 'barLib', groupId: 'com.example', classifier: 'arm7', type: 'zip'))

        assertTrue("should return true because resolved artifact's name and classifier match spec", matches)
    }
}