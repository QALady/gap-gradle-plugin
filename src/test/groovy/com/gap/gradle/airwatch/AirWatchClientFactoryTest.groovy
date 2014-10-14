package com.gap.gradle.airwatch

import org.junit.Assert
import org.junit.Test

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class AirWatchClientFactoryTest {
    @Test
    public void shouldCreateAnAirwatchClientInstanceWithTheCorrectCredentials() throws Exception {
        def factory = new AirWatchClientFactory()
        def credentialProvider = mock(CredentialProvider)
        when(credentialProvider.get("testCredential")).thenReturn(new Credential("testUser", "testPass"))

        def airWatchClient = factory.create(testEnvironment(), credentialProvider)

        Assert.assertEquals("http://api.example.com", airWatchClient.host)
        Assert.assertEquals("testUser", airWatchClient.username)
        Assert.assertEquals("testPass", airWatchClient.password)
        Assert.assertEquals("ABC123", airWatchClient.tenantCode)
    }

    private static Environment testEnvironment() {
        new Environment("foo").with {
            apiHost = "http://api.example.com"
            consoleHost = "http://console.example.com"
            credentialName = "testCredential"
            tenantCode = "ABC123"
            return it
        }
    }
}