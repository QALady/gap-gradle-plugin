package com.gap.pipeline.utils

import org.junit.Test

import static org.hamcrest.CoreMatchers.notNullValue
import static org.junit.Assert.assertThat

class EnvironmentTest {

    @Test
    public void shouldReturnANonEmptyStringForPath(){
        assertThat(new Environment().getValue('PATH'), notNullValue())
    }

}
