package com.gap.gradle.plugins.xcode

import com.gap.gradle.plugins.xcode.exceptions.InvalidXcodeConfigurationException
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Test

import static org.hamcrest.CoreMatchers.containsString
import static org.junit.Assert.assertThat
import static org.mockito.Mockito.mock
import static org.testng.Assert.assertEquals

class XcodeBuildConfigTest {

    private XcodeBuildConfig buildConfig
    private Project project
    private XcodeExtension extension

    @Before
    public void setUp() throws Exception {
        project = ProjectBuilder.builder().build()

        extension = project.extensions.create('xcode', XcodeExtension, mock(Instantiator), project)

        buildConfig = new XcodeBuildConfig()
    }

    @Test
    public void shouldSupportTargetAsClosure() throws Exception {
        buildConfig.target = { 'someTarget' }

        assertEquals(buildConfig.target, 'someTarget')
    }

    @Test
    public void shouldSupportTargetAsString() throws Exception {
        buildConfig.target = 'someTarget'

        assertEquals(buildConfig.target, 'someTarget')
    }

    @Test
    public void shouldSupportSDKAsClosure() throws Exception {
        buildConfig.sdk = { 'someSDK' }

        assertEquals(buildConfig.sdk, 'someSDK')
    }

    @Test
    public void shouldSupportSDKAsString() throws Exception {
        buildConfig.sdk = 'someSDK'

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

        println buildConfig.signingIdentity.class
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

        println buildConfig.signingIdentity.class
        assertEquals(buildConfig.signingIdentity.name, 'testIdentity')
    }

    @Test
    public void shouldThrowValidationExceptionIfInvalid() throws Exception {
        buildConfig = new XcodeBuildConfig()

        try {
            buildConfig.validate()
        } catch (InvalidXcodeConfigurationException e) {
            assertThat(e.message, containsString('target'))
            assertThat(e.message, containsString('SDK'))
            assertThat(e.message, containsString('signing'))
        }
    }
}
