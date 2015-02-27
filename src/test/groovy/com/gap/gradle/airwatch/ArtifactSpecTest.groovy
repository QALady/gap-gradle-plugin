package com.gap.gradle.airwatch

import org.hamcrest.Matchers
import org.junit.Test

import static org.junit.Assert.assertThat

class ArtifactSpecTest {
    @Test
    public void shouldReturnTrueIfAtLeastOneOfTheSpecsWereDefined() throws Exception {
        def spec = new ArtifactSpec()
        spec.classifier = "foo"

        assertThat(spec.hasAtLeastOneSpecDefined(), Matchers.is(true))
    }

    @Test
    public void shouldReturnFalseIfNoneOfTheSpecsWereDefined() throws Exception {
        def spec = new ArtifactSpec()

        assertThat(spec.hasAtLeastOneSpecDefined(), Matchers.is(false))
    }
}
