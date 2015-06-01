package com.gap.gradle.plugins.airwatch

import com.gap.gradle.plugins.airwatch.exceptions.AirWatchClientException
import groovy.mock.interceptor.MockFor
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.apache.http.Header
import org.junit.Before
import org.junit.Ignore
import org.junit.Test

import static groovy.json.JsonOutput.toJson
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static java.lang.String.format
import static org.junit.Assert.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class AirWatchClientTest {

    private AirWatchClient client
    def httpBuilderMock
    def params = []
    def headerMock

    @Before
    void setUp() {
        client = new AirWatchClient("", "someUser", "somePass", "someApiKey")

        httpBuilderMock = new MockFor(HTTPBuilder)

        params = []

        httpBuilderMock.demand.request(1) { Method method, Closure req ->
            req.delegate = [response: [:], uri: [:]]
            req.call()
            params = [method : method,
                      path   : req.uri.path,
                      type   : req.requestContentType,
                      headers: req.headers,
                      query  : req.uri.query,
                      body   : req.body]
        }

        headerMock = mock(Header)
        when(headerMock.name).thenReturn('Content-Type')
        when(headerMock.value).thenReturn('application/json')
    }

    @Test
    void shouldCallUploadChunkEndpoint() {
        httpBuilderMock.use {
            client.uploadChunk("someId", "someEncodedString", 1, 10500, 420)
        }

        assertEquals("API/v1/mam/apps/internal/uploadchunk", params["path"].toString())
        assertEquals(POST, params["method"])
        assertEquals(JSON, params["type"])

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

        assertEquals(smartGroupName, params["query"]["name"])
        assertEquals(locationGroupId, params["query"]["organizationgroupid"])
    }

    @Test
    void shouldCallSearchApplicationEndpoint() {
        SearchApplicationConfig testConfig = new SearchApplicationConfig()
        testConfig.bundleId = "com.test.me"
        testConfig.status = "Active"

        httpBuilderMock.use {
            client.searchApplication(testConfig)
        }

        assertEquals("API/v1/mam/apps/search", params["path"].toString())
        assertEquals(GET, params["method"])
        assertEquals(JSON, params["type"])
        assertEquals("com.test.me", params["query"]["bundleid"])
        assertEquals("Active", params["query"]["status"])
        assertNull(params["query"]["type"])
    }

    @Test
    void shouldCallRetireApplicationEndpoint() {
        String applicationId = "1234"
        httpBuilderMock.use {
            client.retireApplication(applicationId)
        }

        assertEquals("API/v1/mam/apps/internal/1234/retire", params["path"].toString())
        assertEquals(POST, params["method"])
        assertEquals(JSON, params["type"])
        assertNull(params["body"])
    }

    @Test
    public void shouldThrowExceptionIfSmartGroupNotFound() throws Exception {
        def httpMock = new MockFor(HTTPBuilder)

        def successResponse = ['statusLine': ['protocol': 'HTTP/1.1', 'statusCode': 204, 'status': 'No smart group found']]
        def response = ''

        httpMock.demand.request(1) { Method method, Closure req ->
            req.delegate = [response: [:], uri: [:]]
            req.call()

            req.delegate.response.success(successResponse, response)
        }

        httpMock.use {
            try {
                client.smartGroupSearch("notFound", "123")
                fail("Should have thrown exception")
            } catch (AirWatchClientException e) {
            }
        }
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
        assertEquals(null, params["body"])
    }

    @Test
    void shouldCallQueryDeviceByUDIDEndpoint() {
        String deviceUdid = "abc123"
        String path = format("API/v1/mdm/devices/udid/%s/query", deviceUdid)

        httpBuilderMock.use {
            client.queryDevice(deviceUdid)
        }

        assertEquals(path, params["path"].toString())
        assertEquals(POST, params["method"])
        assertEquals(JSON, params["type"])
        assertEquals(null, params["body"])
    }

    @Test
    public void shouldReturnDeviceAppsIndexedByAppIdentifier() throws Exception {
        def deviceUdid = "abc123"
        def httpMock = new MockFor(HTTPBuilder)

        def successResponse = ['statusLine': ['protocol': 'HTTP/1.1', 'statusCode': 200, 'status': 'OK'], headers: headerMock]
        def responseJson = ['DeviceApps': [
                ["ApplicationIdentifier": "com.foo", "BuildVersion": "1.2.3"],
                ["ApplicationIdentifier": "com.bar", "BuildVersion": "6.5.4"]
        ]]

        httpMock.demand.request(1) { Method method, Closure req ->
            req.delegate = [response: [:], uri: [:]]
            req.call()

            assertEquals(GET, method)
            assertEquals(format("API/v1/mdm/devices/udid/%s/apps", deviceUdid), req.uri.path)

            req.delegate.response.success(successResponse, responseJson)
        }

        httpMock.use {
            def result = client.getDeviceApps(deviceUdid)

            assertEquals(2, result.keySet().size())
            assertEquals("1.2.3", result["com.foo"]["BuildVersion"])
            assertEquals("6.5.4", result["com.bar"]["BuildVersion"])
        }
    }

    @Test
    void shouldCallSmartGroupEndpointOnceForEachConfiguredSmartGroup() {
        String smartGroups = "group1,group2, group3"
        String appId = "someAppId"
        String locationGroupId = "someLocationGroupdId"

        def httpMock = new MockFor(HTTPBuilder)

        def successResponse = ['statusLine': ['protocol': 'HTTP/1.1', 'statusCode': 200, 'status': 'OK'], 'headers': headerMock]
        def responseJson = ['SmartGroups': ['SmartGroupID': ["someGroupId"]]]

        httpMock.demand.request(6) { Method method, Closure req ->
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

    @Ignore // FIXME pending while we work on a fix for the json parsing issue (MPL-406)
    @Test
    void shouldThrowAirWatchClientExceptionAndParseJsonResponse() throws Exception {
        def httpMock = new MockFor(HTTPBuilder)

        def errorResponse = [
                'statusLine': ['protocol': 'HTTP/1.1', 'statusCode': 500, 'status': 'Internal Server Error'],
                'headers'   : headerMock
        ]
        def errorBody = ['anError': 'An error message']

        httpMock.demand.request { Method method, Closure req ->
            req.delegate = [uri: [:], response: [:]]
            req.call()
            req.delegate.response.failure(errorResponse, errorBody)
        }

        httpMock.use {
            try {
                client.uploadChunk("", "", 1, 1, 1)
            } catch (AirWatchClientException e) {
                assertTrue("Should contain HTTP statusLine", e.message.contains(errorResponse.statusLine.toString()))
                assertTrue("Should contain response JSON", e.message.contains(toJson(errorBody)))
            }
        }
    }

    @Ignore // FIXME pending while we work on a fix for the json parsing issue (MPL-406)
    @Test
    void shouldNotParseResponseBodyIfContentTypeNotJSON() throws Exception {
        def httpMock = new MockFor(HTTPBuilder)

        def headerMock = mock(Header)
        when(headerMock.name).thenReturn('Content-Type')
        when(headerMock.value).thenReturn('text/html')

        def response = [
                'statusLine': ['protocol': 'HTTP/1.1', 'statusCode': 401, 'status': 'Unauthorized'],
                'headers'   : headerMock
        ]
        def body = 'Some text body'

        httpMock.demand.request { Method method, Closure req ->
            req.delegate = [uri: [:], response: [:]]
            req.call()
            req.delegate.response.failure(response, body)
        }

        httpMock.use {
            try {
                client.uploadChunk("", "", 1, 1, 1)
            } catch (AirWatchClientException e) {
                assertTrue("Should contain HTTP statusLine", e.message.contains(response.statusLine.toString()))
                assertTrue("Should contain text response body", e.message.contains(body))
            }
        }
    }

    @Test
    void shouldThrowAirwatchExceptionIfStatusCodeNotOK() throws Exception {
        def httpMock = new MockFor(HTTPBuilder)

        def response = ['statusLine': ['protocol': 'HTTP/1.1', 'statusCode': 204, 'status': 'No Content']]

        httpMock.demand.request { Method method, Closure req ->
            req.delegate = [uri: [:], response: [:]]
            req.call()
            req.delegate.response.success(response, null)
        }

        httpMock.use {
            try {
                client.uploadChunk("", "", 1, 1, 1)
            } catch (AirWatchClientException e) {
                assertTrue("Should contain HTTP statusLine", e.message.contains(response.statusLine.toString()))
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
