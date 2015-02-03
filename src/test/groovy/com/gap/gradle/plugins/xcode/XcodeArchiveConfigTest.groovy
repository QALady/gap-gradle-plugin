package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertEquals
import static org.testng.Assert.assertEquals

class XcodeArchiveConfigTest {

    private XcodeArchiveConfig archiveConfig

    @Before
    public void setUp() throws Exception {
        archiveConfig = new XcodeArchiveConfig()
    }

    @Test
    public void shouldSupportPropertiesAsString() throws Exception {
        archiveConfig.version = '1'
        archiveConfig.shortVersionString = '1'

        assertEquals(archiveConfig.version, '1')
        assertEquals(archiveConfig.shortVersionString, '1')
    }

    @Test
    public void shouldSupportPropertiesAsClosure() throws Exception {
        archiveConfig.version = { '1' }
        archiveConfig.shortVersionString = { '1' }

        assertEquals(archiveConfig.version, '1')
        assertEquals(archiveConfig.shortVersionString, '1')
    }

    @Test
    public void shouldThrowValidationExceptionIfOptionsAreNotSet() throws Exception {
        try {
            archiveConfig.validate()
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('version'))
            assertThat(e.message, containsString('shortVersionString'))
        }
    }

    @Test
    public void shouldThrowValidationExceptionIfOptionsAreEmpty() throws Exception {
        archiveConfig.version = ''
        archiveConfig.shortVersionString = ''

        try {
            archiveConfig.validate()
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('version'))
            assertThat(e.message, containsString('shortVersionString'))
        }
    }
}
