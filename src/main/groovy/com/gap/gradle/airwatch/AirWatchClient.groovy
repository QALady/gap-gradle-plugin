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
  private String tenantCode

  AirWatchClient(String host, String username, String password, String tenantCode) {
    this.restClient = new RESTClient("${host}/${API_PATH}")
    this.tenantCode = tenantCode

    restClient.auth.basic username, password
  }

  Map uploadApp(File ipaFile, String appName, String appDescription, String locationGroupId) {
    def transactionId = uploadFile(ipaFile)

    println "\nWill create app in AirWatch using the uploaded chunks..."

    beginInstall(transactionId, appName, appDescription, locationGroupId)
  }

  String uploadFile(File file) {
    def fileSize = file.size()
    def chunkSize = 5000
    def chunkSequenceNumber = 1
    def transactionId = "0"
    def totalChunks = "${new BigDecimal( Math.ceil(fileSize / chunkSize) )}"

    println "\nWill upload \"${file.name}\" to AirWatch..."

    file.eachByte(chunkSize) { buffer, sizeRead ->
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

  Map beginInstall(String transactionId, String appName, String appDescription, String locationGroupId) {
    def body = [
      "TransactionId": transactionId,
      "ApplicationName": appName,
      "Description": appDescription,
      "AutoUpdateVersion": true,
      "DeviceType": "Apple",
      "PushMode": "Auto",
      "EnableProvisioning": false,
      "LocationGroupId": locationGroupId,
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
      "aw-tenant-code": this.tenantCode
    ]
    logger.debug "Request headers: ${headers}"

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
      def iterator = e.response.data.childNodes()
      def msg = iterator.collect { "\"$it.name\":\"$it\"" }.join(", ")
      throw new PublishException("AirWatch response was: {${msg}}", e)
    }
  }
}
