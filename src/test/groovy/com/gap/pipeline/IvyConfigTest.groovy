package com.gap.pipeline
import com.gap.pipeline.ec.CommanderClient
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class IvyConfigTest {
    @Test
    public void shouldReturnWhatYouHaveSet() throws Exception {
        def ivyConfig = new IvyConfig()

        ivyConfig.userName = "foo"
        ivyConfig.password = "bar"

        assertEquals("foo", ivyConfig.userName)
        assertEquals("bar", ivyConfig.password)
    }

    @Test
    public void shouldDefaultToValuesFromEC() throws Exception {
        def ivyConfig = new IvyConfig()
        ivyConfig.ecclient = mock(CommanderClient)

        when(ivyConfig.ecclient.getArtifactoryUserName()).thenReturn("foo")
        when(ivyConfig.ecclient.getArtifactoryPassword()).thenReturn("bar")

        assertEquals("foo", ivyConfig.userName)
        assertEquals("bar", ivyConfig.password)
    }
}
