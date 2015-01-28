package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat

class XcodeArchiveConfigTest {

    @Test
    public void shouldThrowValidationExceptionIfInvalid() throws Exception {
        XcodeArchiveConfig archiveConfig = new XcodeArchiveConfig()

        try {
            archiveConfig.validate()
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('version'))
            assertThat(e.message, containsString('shortVersionString'))
        }
    }
}
