package com.gap.gradle.plugins.airwatch
import com.gap.pipeline.ec.CommanderClient
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class CredentialProviderTest {
    @Test
    public void shouldReturnCredential() throws Exception {
        def commanderClient = mock(CommanderClient)
        when(commanderClient.getCredential("/projects/WM Credentials/credentials/testCredential", "userName")).thenReturn("testUser")
        when(commanderClient.getCredential("/projects/WM Credentials/credentials/testCredential", "password")).thenReturn("testPass")

        def provider = new CredentialProvider(commanderClient)
        def credential = provider.get("testCredential")

        assertEquals("testUser", credential.username)
        assertEquals("testPass", credential.password)
    }
}
