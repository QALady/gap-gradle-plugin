package com.gap.gradle.airwatch

import groovy.mock.interceptor.MockFor
import groovyx.net.http.ContentType
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.junit.Before
import org.junit.Test

import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static java.lang.String.format
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue

class AirWatchClientTest {

    private AirWatchClient client
    def httpBuilderMock
    def params = []

    @Before
    void setUp() {
        client = new AirWatchClient("", "someUser", "somePass", "someKey")

        httpBuilderMock = new MockFor(HTTPBuilder)

        params = []

        httpBuilderMock.demand.request(1) { Method method, ContentType contentType, Closure req ->
            req.delegate = [response: [:], uri: [:]]
            req.call()
            params = [method: method, type: contentType, path: req.uri.path,
                      headers: req.headers, query: req.uri.query, body: req.body]
        }
    }

    @Test
    void shouldSendAuthorizationHeaders() {
        def encodedCredentials = "someUser:somePass".getBytes().encodeBase64().toString()

        httpBuilderMock.use {
            client.uploadChunk("someId", "someEncodedString", 1, 10500, 420)
        }

        assertEquals("Basic ${encodedCredentials}", params["headers"]["Authorization"])
    }

    @Test
    void shouldCallUploadChunkEndpoint() {
        httpBuilderMock.use {
            client.uploadChunk("someId", "someEncodedString", 1, 10500, 420)
        }

        assertEquals("API/v1/mam/apps/internal/uploadchunk", params["path"].toString())
        assertEquals(POST, params["method"])
        assertEquals(JSON, params["type"])

        assertEquals("someKey", params["headers"]["aw-tenant-code"])

        assertEquals("someId", params["body"]["TransactionId"])
        assertEquals("someEncodedString", params["body"]["ChunkData"])
        assertEquals(1, params["body"]["ChunkSequenceNumber"])
        assertEquals(420, params["body"]["ChunkSize"])
        assertEquals(10500, params["body"]["TotalApplicationSize"])
    }

    @Test
    void shouldCallBeginInstallEndpoint() {
        httpBuilderMock.use {
            client.beginInstall("someId", new StubConfig("someName", "someDescription", "123", "auto"))
        }

        assertEquals("API/v1/mam/apps/internal/begininstall", params["path"].toString())
        assertEquals(POST, params["method"])
        assertEquals(JSON, params["type"])

        assertEquals("someKey", params["headers"]["aw-tenant-code"])

        assertEquals("someId", params["body"]["TransactionId"])
        assertEquals("someName", params["body"]["ApplicationName"])
        assertEquals("someDescription", params["body"]["Description"])
        assertEquals("123", params["body"]["LocationGroupId"])
        assertEquals("auto", params["body"]["PushMode"])
    }

    @Test
    void shouldCallSmartGroupSearchEndpoint() {
        def smartGroupName = "MPL"
        def locationGroupId = "123"

        httpBuilderMock.use {
            client.smartGroupSearch(smartGroupName, locationGroupId)
        }

        assertEquals("API/v1/mdm/smartgroups/search", params["path"].toString())
        assertEquals(GET, params["method"])
        assertEquals(JSON, params["type"])

        assertEquals("someKey", params["headers"]["aw-tenant-code"])

        assertEquals(smartGroupName, params["query"]["name"])
        assertEquals(locationGroupId, params["query"]["organizationgroupid"])
    }

    @Test
    void shouldCallAddSmartGroupEndpoint() {
        String smartGroupId = "someGroupId"
        String appId = "someAppId"
        String path = format("API/v1/mam/apps/internal/%s/addsmartgroup/%s", appId, smartGroupId)

        httpBuilderMock.use {
            client.addSmartGroup(appId, smartGroupId)
        }

        assertEquals(path, params["path"].toString())
        assertEquals(POST, params["method"])
        assertEquals(JSON, params["type"])

        assertEquals("someKey", params["headers"]["aw-tenant-code"])

        assertEquals(null, params["body"])
    }

    @Test
    void shouldCallSmartGroupEndpointOnceForEachConfiguredSmartGroup() {
        String smartGroups = "group1,group2, group3"
        String appId = "someAppId"
        String locationGroupId = "someLocationGroupdId"

        def httpMock = new MockFor(HTTPBuilder)

        def successResponse = ['statusLine': ['protocol': 'HTTP/1.1', 'statusCode': 200, 'status': 'OK']]
        def responseJson = ['SmartGroups': ['SmartGroupID': ["someGroupId"]]]

        httpMock.demand.request(6) { Method method, ContentType contentType, Closure req ->
            req.delegate = [response: [:], uri: [:]]
            req.call()

            if (method == GET) {
                assertEquals("API/v1/mdm/smartgroups/search", req.uri.path.toString())
            } else if (method == POST) {
                def groupId = responseJson.SmartGroups.SmartGroupID.get(0)
                assertEquals(format("API/v1/mam/apps/internal/%s/addsmartgroup/%s", appId, groupId), req.uri.path)
            }

            req.delegate.response.success(successResponse, responseJson)
        }

        httpMock.use {
            client.assignSmartGroupToApplication(smartGroups, appId, locationGroupId)
        }
    }

    @Test
    void shouldThrowAirWatchClientExceptionAndOutputResponseBodyIfAvailable() throws Exception {
        def httpMock = new MockFor(HTTPBuilder)

        def errorResponse = ['statusLine': ['protocol': 'HTTP/1.1', 'statusCode': 500, 'status': 'Internal Server Error']]
        def errorJson = ['anError': 'An error message']

        httpMock.demand.request { Method method, ContentType contentType, Closure req ->
            req.delegate = [uri: [:], response: [:]]
            req.call()
            req.delegate.response.failure(errorResponse, errorJson)
        }

        httpMock.use {
            try {
                client.uploadChunk("", "", 1, 1, 1)
            } catch (AirWatchClientException e) {
                assertTrue("Should contain HTTP statusLine", e.message.contains(errorResponse.statusLine.toString()))
                assertTrue("Should contain response JSON", e.message.contains(errorJson.toString()))
            }
        }
    }

    private class StubConfig implements BeginInstallConfig {
        String appName
        String appDescription
        String locationGroupId
        String pushMode
        Integer uploadChunks

        StubConfig(String appName, String appDescription, String locationGroupId, String pushMode) {
            this.appName = appName
            this.appDescription = appDescription
            this.locationGroupId = locationGroupId
            this.pushMode = pushMode
        }
    }
}
