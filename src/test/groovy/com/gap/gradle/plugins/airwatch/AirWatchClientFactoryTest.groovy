package com.gap.gradle.plugins.airwatch

import org.junit.Test

import static groovyx.net.http.ContentType.ANY
import static groovyx.net.http.ContentType.JSON
import static org.junit.Assert.assertEquals
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class AirWatchClientFactoryTest {

    def static credentialProvider

    @Test
    public void shouldCreateAnAirwatchClientInstanceWithTheCorrectCredentials() throws Exception {
        credentialProvider = mock(CredentialProvider)
        when(credentialProvider.get("testCredential")).thenReturn(new Credential("testUser", "testPass"))

        def factory = new AirWatchClientFactory()
        def airWatchClient = factory.create(testEnvironment(), credentialProvider)

        def http = airWatchClient.http
        assertEquals('http://api.example.com'.toString(), http.defaultURI.toString())
        assertEquals(ANY, http.defaultContentType)

        def headers = http.headers
        assertEquals('ABC123', headers['aw-tenant-code'])
        assertEquals("Basic ${encodedCredentials()}".toString(), headers['Authorization'])
        assertEquals(JSON.toString(), headers['Accept'])
    }

    private static String encodedCredentials() {
        def credential = credentialProvider.get('testCredential')
        def user = credential.username
        def pass = credential.password

        "${user}:${pass}".getBytes().encodeBase64().toString()
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
