package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.hamcrest.CoreMatchers.equalTo
import static org.junit.Assert.assertThat
import static org.junit.Assert.fail
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when
import static org.testng.Assert.assertEquals

class XcodeBuildConfigTest {

    private XcodeBuildConfig buildConfig
    private Project project
    private xcodeExtension = mock(XcodeExtension)

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()

        buildConfig = new XcodeBuildConfig(xcodeExtension)
    }

    @Test
    public void shouldSupportPropertiesAsClosure() throws Exception {
        buildConfig.target = { 'someTarget' }
        buildConfig.sdk = { 'someSDK' }

        assertEquals(buildConfig.target, 'someTarget')
        assertEquals(buildConfig.sdk, 'someSDK')
    }

    @Test
    public void shouldSupportPropertiesAsString() throws Exception {
        buildConfig.target = 'someTarget'
        buildConfig.sdk = 'someSDK'

        assertEquals(buildConfig.target, 'someTarget')
        assertEquals(buildConfig.sdk, 'someSDK')
    }

    @Test
    public void shouldSupportSigningIdentityAsClosure() throws Exception {
        def identity = new SigningIdentity("testIdentity").with {
            description = 'foo'
            certificateURI = 'bar'
            certificatePassword = 'foobar'
            mobileProvisionURI = 'barfoo'
            return it
        }

        buildConfig.signingIdentity = { identity }

        assertEquals(buildConfig.signingIdentity.name, 'testIdentity')
    }

    @Test
    public void shouldSupportSigningIdentityAsObject() throws Exception {
        def identity = new SigningIdentity("testIdentity").with {
            description = 'foo'
            certificateURI = 'bar'
            certificatePassword = 'foobar'
            mobileProvisionURI = 'barfoo'
            return it
        }

        buildConfig.signingIdentity = identity

        assertEquals(buildConfig.signingIdentity.name, 'testIdentity')
    }

    @Test
    public void shouldThrowValidationExceptionIfOptionsAreNotSet() throws Exception {
        try {
            buildConfig.validate()
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('target'))
            assertThat(e.message, containsString('SDK'))
        }
    }

    @Test
    public void shouldThrowValidationExceptionIfOptionsAreEmpty() throws Exception {
        buildConfig.target = ''
        buildConfig.sdk = ''

        try {
            buildConfig.validate()
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('target'))
            assertThat(e.message, containsString('SDK'))
        }
    }

    @Test
    public void shouldNotThrowValidationExceptionIfTargetIsNotRequired() throws Exception {
        when(xcodeExtension.isTargetRequired()).thenReturn(false)
        buildConfig.sdk = 'foo'
        buildConfig.signingIdentity = mock(SigningIdentity)

        buildConfig.validate()
    }

    @Test
    public void shouldUseTargetAsProductNameIfNotExplicitlyDefined() throws Exception {
        buildConfig.target = 'foo'

        assertThat(buildConfig.productName, equalTo('foo'))
    }

    @Test
    public void shouldReturnSpecifiedProductName() throws Exception {
        buildConfig.productName = 'foo'

        assertThat(buildConfig.productName, equalTo('foo'))
    }

    @Test
    public void shouldUseSchemeAsProductNameIfTargetWasNotSpecified() throws Exception {
        when(xcodeExtension.getScheme()).thenReturn("scheme name")
        buildConfig.target = null

        assertThat(buildConfig.productName, equalTo("scheme name"))
    }

    @Test
    public void shouldReturnSpecifiedXcodeBuildConfiguration() throws Exception {
        buildConfig.configuration = 'TestConfig'

        assertEquals(buildConfig.configuration, 'TestConfig')
    }

    @Test
    public void shouldReturnDefaultXcodeBuildConfigurationIfNotSpecified() throws Exception {
        assertEquals(buildConfig.configuration, 'Release')
    }

    @Test
    public void shouldReturnSchemeIfTargetWasNotSpecified() throws Exception {
        buildConfig.target = null
        when(xcodeExtension.getScheme()).thenReturn("scheme name")

        assertThat(buildConfig.target, equalTo("scheme name"))
    }

    @Test
    public void testAllPropertiesShouldBeNullSafe() throws Exception {
        def config = buildConfig

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
