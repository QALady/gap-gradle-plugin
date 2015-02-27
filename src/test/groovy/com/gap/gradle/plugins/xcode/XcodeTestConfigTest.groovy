package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.gradle.api.Project
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.CoreMatchers.instanceOf
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static org.testng.Assert.assertEquals

public class XcodeTestConfigTest {

    private XcodeTestConfig testConfig
    private XcodeExtension xcodeExtension = mock(XcodeExtension)

    @Before
    public void setUp() throws Exception {
        Project project = ProjectBuilder.builder().build()
        Instantiator instantiator = ((ProjectInternal) project).getServices().get(Instantiator)

        testConfig = new XcodeTestConfig(instantiator, xcodeExtension)
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
    public void shouldReturnXcodeExtensionSchemeIfTestOneNotSpecified() throws Exception {
        when(xcodeExtension.getScheme()).thenReturn('xcode extension scheme')
        testConfig.scheme = null

        assertEquals(testConfig.scheme, 'xcode extension scheme')
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

    @Test
    public void testAllPropertiesShouldBeNullSafe() throws Exception {
        def config = testConfig

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
