package com.gap.gradle.plugins.airwatch

import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

public class SearchApplicationConfigTest {
    private final SearchApplicationConfig config = new SearchApplicationConfig()

    @Test
    public void shouldReturnTrueIfHasAnySearchParamDefined() throws Exception {
        this.config.applicationName = "foo"

        assertTrue(this.config.hasSearchParams())
    }

    @Test
    public void shouldReturnFalseIfThereAreNoSearchParamsDefined() throws Exception {
        assertFalse(config.hasSearchParams())
    }

    @Test
    public void shouldReturnOnlyDefinedSearchParams() throws Exception {
        config.bundleId = "com.foo"

        assertEquals(["bundleid": "com.foo"], config.validSearchEndPointParams())
    }
}
