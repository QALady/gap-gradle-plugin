package com.gap.gradle.plugins.airwatch
import org.hamcrest.Matchers
import org.junit.Test

import static org.hamcrest.Matchers.equalTo
import static org.junit.Assert.assertThat

class ArtifactSpecTest {
    private spec = new ArtifactSpec()

    @Test
    public void shouldReturnTrueIfAtLeastOneOfTheSpecsWereDefined() throws Exception {
        spec.classifier = "foo"

        assertThat(spec.hasAtLeastOneSpecDefined(), Matchers.is(true))
    }

    @Test
    public void shouldReturnFalseIfNoneOfTheSpecsWereDefined() throws Exception {
        assertThat(spec.hasAtLeastOneSpecDefined(), Matchers.is(false))
    }

    @Test
    public void shouldHaveToStringPrintingTheDefinedValues() throws Exception {
        spec.classifier = "foo"
        assertThat(spec.toStringWithDefinedValues(), equalTo("artifact spec { classifier='foo' }"))
    }

    @Test
    public void shouldHaveToStringPrintEmptyIfNoValuesDefined() throws Exception {
        assertThat(spec.toStringWithDefinedValues(), equalTo("artifact spec is empty"))
    }
}
