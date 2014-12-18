package com.gap.gradle.airwatch

import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static groovy.json.JsonOutput.toJson
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static java.lang.Math.ceil
import static java.lang.String.format

class AirWatchClient {

    private static final Logger logger = LoggerFactory.getLogger(AirWatchClient)

    private static final API_V1_PATH = "API/v1"

    private static final MAM_APPS_PATH = "${API_V1_PATH}/mam/apps/internal"
    private static final UPLOAD_CHUNK_PATH = "${MAM_APPS_PATH}/uploadchunk"
    private static final BEGIN_INSTALL_PATH = "${MAM_APPS_PATH}/begininstall"
    private static final ADD_SMARTGROUP_PATH = "${MAM_APPS_PATH}/%s/addsmartgroup/%s"

    private static final MDM_SMARTGROUPS_PATH = "${API_V1_PATH}/mdm/smartgroups"
    private static final SMARTGROUPS_SEARCH_PATH = "${MDM_SMARTGROUPS_PATH}/search"

    private HTTPBuilder http
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

        this.http = new HTTPBuilder(host)
    }

    Map uploadApp(File ipaFile, BeginInstallConfig config) {
        def transactionId = uploadFile(ipaFile, config)

        println "\nCreating the app in AirWatch using the uploaded chunks..."

        beginInstall(transactionId, config)
    }

    String uploadFile(File file, BeginInstallConfig config) {
        def fileSize = file.size()
        def chunkSequenceNumber = 1
        def transactionId = "0"
        def chunkSize = (ceil(fileSize / config.uploadChunks)).intValue()

        println "\nWill upload \"${file.name}\" to AirWatch..."

        file.eachByte(chunkSize) { buffer, sizeRead ->
            def bufferSlice = Arrays.copyOfRange(buffer, 0, sizeRead)
            def encodedChunk = bufferSlice.encodeBase64().toString()

            println "\nUploading chunk ${chunkSequenceNumber} of ${config.uploadChunks}..."

            def response = uploadChunk(transactionId, encodedChunk, chunkSequenceNumber, fileSize, chunkSize)
            transactionId = response.get("TranscationId")

            chunkSequenceNumber++
        }

        transactionId
    }

    Map uploadChunk(String transactionId, String encodedChunk, int chunkSequenceNumber, long fileSize, int chunkSize) {
        Map args = [
                "path": UPLOAD_CHUNK_PATH,
                "body": [
                        "TransactionId"       : transactionId,
                        "ChunkData"           : encodedChunk,
                        "ChunkSequenceNumber" : chunkSequenceNumber,
                        "TotalApplicationSize": fileSize,
                        "ChunkSize"           : chunkSize
                ]
        ]

        doRequest(POST, args)
    }

    Map beginInstall(String transactionId, BeginInstallConfig config) {
        Map args = [
                "path": BEGIN_INSTALL_PATH,
                "body": [
                        "TransactionId"     : transactionId,
                        "ApplicationName"   : config.appName,
                        "Description"       : config.appDescription,
                        "AutoUpdateVersion" : true,
                        "DeviceType"        : "Apple",
                        "PushMode"          : config.pushMode,
                        "EnableProvisioning": false,
                        "LocationGroupId"   : config.locationGroupId,
                        "SupportedModels"   : [
                                "Model": [
                                        ["ModelId": 1, "ModelName": "iPhone"],
                                        ["ModelId": 2, "ModelName": "iPad"],
                                        ["ModelId": 3, "ModelName": "iPod Touch"]
                                ]
                        ]
                ]
        ]

        doRequest(POST, args)
    }

    void assignSmartGroupToApplication(String smartGroups, String appId, String locationGroupId) {
        def smartGroupNames = smartGroups.split(/,\s?/)

        smartGroupNames.each { name ->
            def searchResult = smartGroupSearch(name, locationGroupId)
            String smartGroupId = searchResult["SmartGroups"]["SmartGroupID"].get(0)

            addSmartGroup(appId, smartGroupId)
        }
    }

    Map smartGroupSearch(String smartGroupName, String locationGroupId) {
        println "\nSearching for Smart Group \"${smartGroupName}\"..."

        Map args = [
                "path" : SMARTGROUPS_SEARCH_PATH,
                "query": ["name": smartGroupName, "organizationgroupid": locationGroupId]
        ]

        doRequest(GET, args)
    }

    void addSmartGroup(String appId, String smartGroupId) {
        println "\nAssigning Smart Group id \"${smartGroupId}\" to app id \"${appId}\"..."

        Map args = [
                "path": format(ADD_SMARTGROUP_PATH, appId, smartGroupId)
        ]

        doRequest(POST, args)
    }

    private Map doRequest(Method method, Map params) {

        http.request(method, JSON) { req ->
            uri.path = params.get("path")
            headers = defaultHeaders()

            if (params.containsKey("body")) {
                body = params.get("body")
                logger.debug("Request body: {}", params.get("body"))
            }

            if (params.containsKey("query")) {
                uri.query = params.get("query")
            }

            response.success = { resp, body ->
                println "AirWatch returned a successful response: ${resp.statusLine}" +
                        "\n" +
                        formattedResponseBody(body)

                return body
            }

            response.failure = { resp, body ->
                def errorMessage = "AirWatch returned an unexpected error: ${resp.statusLine}" +
                        "\n" +
                        formattedResponseBody(body)

                throw new AirWatchClientException(errorMessage)
            }
        }
    }

    private String formattedResponseBody(Map body) {
        def message = "Response body is"

        message <<= (body == null) ? ' empty' : ": ${toJson(body)}"

        return message
    }

    private Map<String, String> defaultHeaders() {
        [
                "Content-Type"  : "application/json",
                "aw-tenant-code": this.tenantCode,
                "Authorization" : "Basic $encodedCredentials"
        ]
    }
}
