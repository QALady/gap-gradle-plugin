package com.gap.gradle.plugins.airwatch

import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail

class BeginInstallConfigValidatorTest {
    private config = new FakeConfig()
    private validator = new BeginInstallConfigValidator()

    @Test
    public void shouldValidateBlankAppName() throws Exception {
        config.appName = ""

        try {
            validator.validate(config)
        } catch (RuntimeException e) {
            assertEquals("Please configure `appName`.", e.message)
        }
    }

    @Test
    public void shouldValidateBlankAppDescription() throws Exception {
        config.appDescription = ""

        try {
            validator.validate(config)
        } catch (RuntimeException e) {
            assertEquals("Please configure `appDescription`.", e.message)
        }
    }

    @Test
    public void shouldValidateBlankLocationGroupId() throws Exception {
        config.locationGroupId = ""

        try {
            validator.validate(config)
        } catch (RuntimeException e) {
            assertEquals("Please configure `locationGroupId` for the environment.", e.message)
        }
    }

    @Test
    public void shouldValidateBlankPushMode() throws Exception {
        config.pushMode = ""

        try {
            validator.validate(config)
        } catch (RuntimeException e) {
            assertEquals("Please configure `pushMode`.", e.message)
        }
    }

    @Test
    public void shouldValidatePushModeToBeAutoOrOnDemand() throws Exception {
        config.pushMode = "auto"
        validator.validate(config)

        config.pushMode = "OnDemand"
        validator.validate(config)

        config.pushMode = "baz"
        try {
            validator.validate(config)
            fail("should have failed because baz is not a valid pushMode.")
        } catch(RuntimeException e) {
            assertEquals("Invalid `pushMode`, needs to be 'auto' or 'ondemand'.", e.message)
        }
    }

    private class FakeConfig implements BeginInstallConfig {
        String appName = "foo"
        String appDescription = "bar"
        String locationGroupId = "123"
        String pushMode = "auto"
        Integer uploadChunks = 1
    }
}
