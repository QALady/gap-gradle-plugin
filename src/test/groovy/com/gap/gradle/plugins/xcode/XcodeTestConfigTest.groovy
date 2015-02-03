package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.gradle.api.Action
import org.gradle.api.internal.DependencyInjectingInstantiator
import org.gradle.internal.service.ServiceRegistry
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.CoreMatchers.instanceOf
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock
import static org.testng.Assert.assertEquals

public class XcodeTestConfigTest {

    private XcodeTestConfig testConfig
    private DependencyInjectingInstantiator instantiator

    @Before
    public void setUp() throws Exception {
        final ServiceRegistry services = mock(ServiceRegistry)
        final Action warning = mock(Action)
        instantiator = new DependencyInjectingInstantiator(services, warning)

        testConfig = new XcodeTestConfig(instantiator)
    }

    @Test
    public void shouldInitializeDestinationConfig() throws Exception {
        assertThat(testConfig.destination, instanceOf(DestinationConfig))
    }

    @Test
    public void shouldSupportSchemeAsString() throws Exception {
        testConfig.scheme = 'some scheme'

        assertEquals(testConfig.scheme, 'some scheme')
    }

    @Test
    public void shouldSupportSchemeAsClosure() throws Exception {
        testConfig.scheme = { 'some scheme' }

        assertEquals(testConfig.scheme, 'some scheme')
    }

    @Test
    public void shouldThrowValidationExceptionIfOptionsAreNotSet() throws Exception {
        try {
            testConfig.validate()
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('scheme'))
        }
    }

    @Test
    public void shouldThrowValidationExceptionIfOptionsAreEmpty() throws Exception {
        try {
            testConfig.validate()
            testConfig.scheme = ''
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('scheme'))
        }
    }
}
