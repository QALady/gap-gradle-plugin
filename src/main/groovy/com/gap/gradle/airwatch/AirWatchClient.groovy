package com.gap.gradle.airwatch

import static groovyx.net.http.ContentType.JSON

import groovy.json.JsonBuilder
import groovyx.net.http.HttpResponseException
import groovyx.net.http.RESTClient
import org.gradle.api.artifacts.PublishException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class AirWatchClient {

  private static final Logger logger = LoggerFactory.getLogger(AirWatchClient)

  private static final API_PATH = "API/v1/mam/apps/internal/"
  private static final UPLOAD_CHUNK_PATH = "uploadchunk"
  private static final BEGIN_INSTALL_PATH = "begininstall"
  private static final CHUNK_SIZE = 5000

  private RESTClient restClient
  final String host
  final String username
  final String password
  final String tenantCode
  final String encodedCredentials

  AirWatchClient(String host, String username, String password, String tenantCode) {
    this.host = host
    this.username = username
    this.password = password
    this.tenantCode = tenantCode
    this.encodedCredentials = "$username:$password".getBytes().encodeBase64().toString()

    this.restClient = new RESTClient("${host}/${API_PATH}")
  }

  Map uploadApp(File ipaFile, BeginInstallConfig config) {
    def transactionId = uploadFile(ipaFile)

    println "\nWill create app in AirWatch using the uploaded chunks..."

    beginInstall(transactionId, config)
  }

  String uploadFile(File file) {
    def fileSize = file.size()
    def chunkSequenceNumber = 1
    def transactionId = "0"
    def totalChunks = "${new BigDecimal( Math.ceil(fileSize / CHUNK_SIZE) )}"

    println "\nWill upload \"${file.name}\" to AirWatch..."

    file.eachByte(CHUNK_SIZE) { buffer, sizeRead ->
      def bufferSlice = Arrays.copyOfRange(buffer, 0, sizeRead)
      def encodedChunk = bufferSlice.encodeBase64().toString()

      println "Uploading chunk ${chunkSequenceNumber} of ${totalChunks}..."

      transactionId = uploadChunk(transactionId, encodedChunk, chunkSequenceNumber, fileSize)

      chunkSequenceNumber++
    }

    transactionId
  }

  String uploadChunk(String transactionId, String encodedChunk, int chunkSequenceNumber, long fileSize) {
    def body = [
      "TransactionId": transactionId,
      "ChunkData": encodedChunk,
      "ChunkSequenceNumber": chunkSequenceNumber,
      "TotalApplicationSize": fileSize,
      "ChunkSize": CHUNK_SIZE
    ]

    def response = doPost(UPLOAD_CHUNK_PATH, body)

    response.get("TranscationId")
  }

  Map beginInstall(String transactionId, BeginInstallConfig config) {
    def body = [
      "TransactionId": transactionId,
      "ApplicationName": config.appName,
      "Description": config.appDescription,
      "AutoUpdateVersion": true,
      "DeviceType": "Apple",
      "PushMode": config.pushMode,
      "EnableProvisioning": false,
      "LocationGroupId": config.locationGroupId,
      "SupportedModels": [
        "Model": [
          [ "ModelId": 1, "ModelName": "iPhone" ],
          [ "ModelId": 2, "ModelName": "iPad" ],
          [ "ModelId": 3, "ModelName": "iPod Touch" ]
        ]
      ]
    ]

    doPost(BEGIN_INSTALL_PATH, body)
  }

  private Map doPost(String resourcePath, Map requestBody) {
    def headers = [
      "Content-Type": "application/json",
      "aw-tenant-code": this.tenantCode,
      "Authorization": "Basic $encodedCredentials"
    ]

    def body = new JsonBuilder(requestBody)
    logger.debug "Request body: ${body}"

    try {
      def response = restClient.post(
        path: resourcePath,
        headers: headers,
        body: body.toString(),
        requestContentType: JSON)

        println "AirWatch response was ${response.data}"

        response.data
    } catch(HttpResponseException e) {
        println "Airwatch responded with an HTTP error, status code: ${e.response.status}"
        println "Request path: ${resourcePath}"
        println "Request body: ${body}"
        println "Response headers: ${e.response.allHeaders}"

        def errorMessage = "Airwatch responded with an HTTP error"
        def parsedResponse = e.response.data
        if (parsedResponse != null) {
            def iterator = parsedResponse.childNodes()
            def msg = iterator.collect { "\"$it.name\":\"$it\"" }.join(", ")
            errorMessage = "AirWatch response was: {${msg}}"
        }

        throw new PublishException(errorMessage, e)
    }
  }
}
