package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail
import static org.testng.Assert.assertEquals

class DestinationConfigTest {

    private DestinationConfig destinationConfig

    @Before
    public void setUp() throws Exception {
        destinationConfig = new DestinationConfig()
    }

    @Test
    public void shouldSupportPropertiesAsString() throws Exception {
        destinationConfig.platform = 'somePlatform'
        destinationConfig.name = 'someName'
        destinationConfig.os = 'someOs'

        assertEquals(destinationConfig.platform, 'somePlatform')
        assertEquals(destinationConfig.name, 'someName')
        assertEquals(destinationConfig.os, 'someOs')
    }

    @Test
    public void shouldSupportPropertiesAsClosure() throws Exception {
        destinationConfig.platform = { 'somePlatform' }
        destinationConfig.name = { 'someName' }
        destinationConfig.os = { 'someOs' }

        assertEquals(destinationConfig.platform, 'somePlatform')
        assertEquals(destinationConfig.name, 'someName')
        assertEquals(destinationConfig.os, 'someOs')
    }

    @Test
    public void shouldThrowValidationExceptionIfOptionsAreNotSet() throws Exception {
        try {
            destinationConfig.validate()
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('platform'))
            assertThat(e.message, containsString('name'))
            assertThat(e.message, containsString('os'))
        }
    }

    @Test
    public void shouldThrowValidationExceptionIfOptionsAreEmpty() throws Exception {
        destinationConfig.platform = ''
        destinationConfig.name = ''
        destinationConfig.os = ''

        try {
            destinationConfig.validate()
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('platform'))
            assertThat(e.message, containsString('name'))
            assertThat(e.message, containsString('os'))
        }
    }

    @Test
    public void testAllPropertiesShouldBeNullSafe() throws Exception {
        def config = destinationConfig

        config.class.declaredFields.findAll { it.type.is(Property) }.each {
            try {
                config.getAt(it.name)
            } catch (NullPointerException e) {
                fail("\"${it.name}\" getter should not throw NullPointerException. " +
                        "Make sure Groovy’s null safe operator is used (e.g.: ${it.name}?.get()).")
            }
        }
    }
}
