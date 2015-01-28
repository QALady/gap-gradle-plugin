package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat

class DestinationConfigTest {

    @Test
    public void shouldThrowValidationExceptionIfInvalid() throws Exception {
        DestinationConfig destinationConfig = new DestinationConfig()

        try {
            destinationConfig.validate()
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('platform'))
            assertThat(e.message, containsString('name'))
            assertThat(e.message, containsString('os'))
        }
    }
}
