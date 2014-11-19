package com.gap.gradle.airwatch
import groovy.json.JsonSlurper
import groovy.mock.interceptor.MockFor
import groovyx.net.http.HttpResponseDecorator
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.gradle.api.artifacts.PublishException
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.assertEquals
import static org.junit.Assert.fail
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class AirWatchClientTest {

    private AirWatchClient client
    def mockRESTClient

    @Before
    void setUp() {
        mockRESTClient = new MockFor(RESTClient)
        client = new AirWatchClient("", "someUser", "somePass", "someKey")
    }

    @Test
    void shouldSendAuthorizationHeaders() {
        def encodedCredentials = "someUser:somePass".getBytes().encodeBase64().toString()
        def expectedAuthHeader = "Basic $encodedCredentials"

        mockRESTClient.demand.post { Map<String, ?> args ->
            assertEquals("uploadchunk", args.path.toString())
            assertEquals(expectedAuthHeader, args.headers.get("Authorization"))

            return [data: [:]]
        }

        mockRESTClient.use {
            client.uploadChunk("someId", "someEncodedString", 1, 10500)
        }
    }

    @Test
    void shouldCallUploadChunkEndpoint() {
        mockRESTClient.demand.post { Map<String, ?> args ->
            assertEquals("uploadchunk", args.path.toString())
            assertEquals("someKey", args.headers.get("aw-tenant-code"))

            def body = new JsonSlurper().parseText(args.body)
            assertEquals("someId", body.get("TransactionId"))
            assertEquals("someEncodedString", body.get("ChunkData"))
            assertEquals(1, body.get("ChunkSequenceNumber"))
            assertEquals(5000, body.get("ChunkSize"))
            assertEquals(10500, body.get("TotalApplicationSize"))

            return [data: [:]]
        }

        mockRESTClient.use {
            client.uploadChunk("someId", "someEncodedString", 1, 10500)
        }
    }

    @Test
    void shouldCallBeginInstallEndpoint() {
        mockRESTClient.demand.post { Map<String, ?> args ->
            assertEquals("begininstall", args.path.toString())
            assertEquals("someKey", args.headers.get("aw-tenant-code"))

            def body = new JsonSlurper().parseText(args.body)
            assertEquals("someId", body.get("TransactionId"))
            assertEquals("someName", body.get("ApplicationName"))
            assertEquals("someDescription", body.get("Description"))
            assertEquals("123", body.get("LocationGroupId"))
            assertEquals("auto", body.get("PushMode"))

            return [data: [:]]
        }

        mockRESTClient.use {
            client.beginInstall("someId", new StubConfig("someName", "someDescription", "123", "auto"))
        }
    }

    @Test
    public void shouldThrowPublishExceptionAndOutputResponseBodyIfAvailable() throws Exception {
        mockRESTClient.demand.post { Map<String, ?> args ->
            def httpResponseDecorator = mock(HttpResponseDecorator)
            def httpResponseException = mock(HttpResponseException)

            when(httpResponseException.response).thenReturn(httpResponseDecorator)
            when(httpResponseDecorator.status).thenReturn(500)
            when(httpResponseDecorator.data).thenReturn(new XmlSlurper().parseText("<error><code>500</code><msg>believe me</msg></error>"))

            throw httpResponseException
        }

        mockRESTClient.use {
            try {
                client.uploadChunk("someId", "someEncodedString", 1, 10500)
                fail("An exception should have been raised")
            } catch (PublishException e) {
                assertEquals("AirWatch response was: {\"code\":\"500\", \"msg\":\"believe me\"}", e.message)
            }
        }
    }

    @Test
    public void shouldThrowPublishExceptionWithoutDetailsIfResponseBodyNotAvailable() throws Exception {
        mockRESTClient.demand.post { Map<String, ?> args ->
            def httpResponseDecorator = mock(HttpResponseDecorator)
            def httpResponseException = mock(HttpResponseException)

            when(httpResponseException.response).thenReturn(httpResponseDecorator)
            when(httpResponseDecorator.status).thenReturn(500)

            throw httpResponseException
        }

        mockRESTClient.use {
            try {
                client.uploadChunk("someId", "someEncodedString", 1, 10500)
                fail("An exception should have been raised")
            } catch (PublishException e) {
                assertEquals("Airwatch responded with an HTTP error", e.message)
            }
        }
    }

    private class StubConfig implements BeginInstallConfig {
        String appName
        String appDescription
        String locationGroupId
        String pushMode

        StubConfig(String appName, String appDescription, String locationGroupId, String pushMode) {
            this.appName = appName
            this.appDescription = appDescription
            this.locationGroupId = locationGroupId
            this.pushMode = pushMode
        }
    }
}
