package com.gap.pipeline.utils

import static org.hamcrest.CoreMatchers.notNullValue
import static org.junit.Assert.assertThat

import org.junit.Test

class EnvironmentTest {

    @Test
    public void shouldReturnANonEmptyStringForPath(){
        assertThat(new Environment().getValue('PATH'), notNullValue())
    }

}
