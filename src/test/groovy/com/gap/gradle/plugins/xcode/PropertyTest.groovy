package com.gap.gradle.plugins.xcode

import org.junit.Test

import static org.testng.Assert.assertEquals

class PropertyTest {

    @Test
    public void shouldReturnValueIfAStringIsGiven() throws Exception {
        def someOption = new Property('some value')

        assertEquals(someOption.get(), 'some value')
    }

    @Test
    public void shouldReturnValueIfAClosureIsGiven() throws Exception {
        def identity = new SigningIdentity('test').with {
            description = 'testing'
        }

        Property someOption = new Property(identity)

        assertEquals(someOption.get(), identity)
    }
}
