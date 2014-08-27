package com.gap.gradle.airwatch

import static org.junit.Assert.assertEquals

import groovyx.net.http.AuthConfig
import groovy.json.JsonSlurper
import groovy.mock.interceptor.MockFor
import groovyx.net.http.RESTClient
import org.junit.Before
import org.junit.Test

class AirWatchClientTest {

  def client
  def mockRESTClient

  @Before
  void setUp() {
    mockRESTClient = new MockFor(RESTClient)
    client = new AirWatchClient("", "", "", "tenantCode")
  }

  @Test
  void shouldAuthenticateOnClientInstantiation() {
    def mockAuthConfig = new MockFor(AuthConfig)

    mockAuthConfig.demand.basic { String username, String password ->
      assertEquals("user", username)
      assertEquals("pass", password)
    }

    mockAuthConfig.use {
      new AirWatchClient("", "user", "pass", "")
    }
  }

  @Test
  void shouldCallUploadChunkEndpoint() {
    mockRESTClient.demand.post { Map<String, ?> args ->
      assertEquals("uploadchunk", args.path.toString())
      assertEquals("tenantCode", args.headers.get("aw-tenant-code"))

      def body = new JsonSlurper().parseText(args.body)
      assertEquals("transactionId", body.get("TransactionId"))
      assertEquals("encodedChunk", body.get("ChunkData"))
      assertEquals(1, body.get("ChunkSequenceNumber"))
      assertEquals(5000, body.get("ChunkSize"))
      assertEquals(10500, body.get("TotalApplicationSize"))

      return [data: [:]]
    }

    mockRESTClient.use {
      client.uploadChunk("transactionId", "encodedChunk", 1, 10500)
    }
  }

  @Test
  void shouldCallBeginInstallEndpoint() {
    mockRESTClient.demand.post { Map<String, ?> args ->
      assertEquals("begininstall", args.path.toString())
      assertEquals("tenantCode", args.headers.get("aw-tenant-code"))

      def body = new JsonSlurper().parseText(args.body)
      assertEquals("transactionId", body.get("TransactionId"))
      assertEquals("applicationName", body.get("ApplicationName"))
      assertEquals("description", body.get("Description"))

      return [data: [:]]
    }

    mockRESTClient.use {
      client.beginInstall("transactionId", "applicationName", "description")
    }
  }

}
