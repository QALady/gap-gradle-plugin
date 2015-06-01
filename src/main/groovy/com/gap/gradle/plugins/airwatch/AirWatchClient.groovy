package com.gap.gradle.plugins.airwatch

import com.gap.gradle.plugins.airwatch.exceptions.AirWatchClientException
import groovyx.net.http.HTTPBuilder
import groovyx.net.http.Method
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import static groovy.json.JsonOutput.toJson
import static groovyx.net.http.ContentType.ANY
import static groovyx.net.http.ContentType.JSON
import static groovyx.net.http.Method.GET
import static groovyx.net.http.Method.POST
import static java.lang.Math.ceil
import static java.lang.String.format
import static java.util.Arrays.copyOfRange
import static java.util.Collections.emptyMap
import static org.apache.commons.lang.StringUtils.isBlank

class AirWatchClient {

    private static final Logger logger = LoggerFactory.getLogger(AirWatchClient)

    private static final API_V1_PATH = "API/v1"

    private static final MAM_APPS_PATH = "${API_V1_PATH}/mam/apps/internal"
    private static final UPLOAD_CHUNK_PATH = "${MAM_APPS_PATH}/uploadchunk"
    private static final BEGIN_INSTALL_PATH = "${MAM_APPS_PATH}/begininstall"
    private static final ADD_SMARTGROUP_PATH = "${MAM_APPS_PATH}/%s/addsmartgroup/%s"

    private static final MDM_SMARTGROUPS_PATH = "${API_V1_PATH}/mdm/smartgroups"
    private static final SMARTGROUPS_SEARCH_PATH = "${MDM_SMARTGROUPS_PATH}/search"

    private static final MDM_QUERY_DEVICE_BY_UDID_PATH = "${API_V1_PATH}/mdm/devices/udid/%s/query"
    private static final MDM_DEVICE_APPS_BY_UDID_PATH = "${API_V1_PATH}/mdm/devices/udid/%s/apps"

    private static final APP_SEARCH_PATH = "${API_V1_PATH}/mam/apps/search"
    private static final APP_RETIRE_PATH = "${MAM_APPS_PATH}/%s/retire"

    private HTTPBuilder http

    AirWatchClient(String host, String username, String password, String tenantCode) {
        def encodedCredentials = "${username}:${password}".getBytes().encodeBase64().toString()

        def headers = [
                'aw-tenant-code': tenantCode,
                'Authorization' : "Basic ${encodedCredentials}",
                'Accept'        : 'application/json'
        ]

        http = new HTTPBuilder(host)
        http.contentType = ANY
        http.headers = headers
    }

    Map uploadApp(File ipaFile, BeginInstallConfig config) {
        def transactionId = uploadFile(ipaFile, config)

        println "Creating the app in AirWatch using the uploaded chunks..."

        beginInstall(transactionId, config)
    }

    String uploadFile(File file, BeginInstallConfig config) {
        long fileSize = file.size()
        int chunkSequenceNumber = 1
        String transactionId = "0"
        int chunkSize = (ceil(fileSize / config.uploadChunks)).intValue()

        file.eachByte(chunkSize) { byte[] buffer, int sizeRead ->
            byte[] bufferSlice = copyOfRange(buffer, 0, sizeRead)
            String encodedChunk = bufferSlice.encodeBase64().toString()

            println "Uploading chunk ${chunkSequenceNumber} of ${config.uploadChunks}..."

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

    Map searchApplication(SearchApplicationConfig config) {
        logger.info config.toString()

        Map args = [
                "path" : APP_SEARCH_PATH,
                "query": config.validSearchEndPointParams()
        ]

        doRequest(GET, args)
    }

    void retireApplication(String appId) {
        println "Retiring an application with id \"${appId}\"..."

        Map args = ["path": format(APP_RETIRE_PATH, appId)]

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

    void queryDevice(String udid) {
        println "\nSending query command to device with UDID $udid..."

        doRequest(POST, ["path": format(MDM_QUERY_DEVICE_BY_UDID_PATH, udid)])
    }

    Map getDeviceApps(String udid) {
        def response = doRequest(GET, ["path": format(MDM_DEVICE_APPS_BY_UDID_PATH, udid)])

        response["DeviceApps"].inject([:]) { result, appInfo ->
            result[appInfo["ApplicationIdentifier"]] = appInfo
            return result
        }
    }

    Map smartGroupSearch(String smartGroupName, String locationGroupId) {
        println "Searching for Smart Group \"${smartGroupName}\"..."

        Map args = [
                "path" : SMARTGROUPS_SEARCH_PATH,
                "query": ["name": smartGroupName, "organizationgroupid": locationGroupId]
        ]

        def response = doRequest(GET, args)

        if (response.isEmpty()) {
            throw new AirWatchClientException("No smart group found with name '$smartGroupName'.")
        }

        response
    }

    void addSmartGroup(String appId, String smartGroupId) {
        println "Assigning Smart Group id \"${smartGroupId}\" to app id \"${appId}\"..."

        Map args = [
                "path": format(ADD_SMARTGROUP_PATH, appId, smartGroupId)
        ]

        doRequest(POST, args)
    }

    private Map doRequest(Method method, Map params) {

        http.request(method) { req ->
            uri.path = params.get("path")
            requestContentType = JSON

            if (params.containsKey("body")) {
                body = params.get("body")
                logger.debug("Request body: {}", toJson(params.get("body")))
            }

            if (params.containsKey("query")) {
                uri.query = params.get("query")
            }

            response.success = { resp, body ->
                logger.info "AirWatch returned a successful response: ${resp.statusLine}\n" +
                        parseResponseBody(body, resp)

                return emptyBody(body) ? emptyMap() : body
            }

            response.failure = { resp, body ->
                throw new AirWatchClientException("AirWatch returned an unexpected error: ${resp.statusLine}\n" +
                        parseResponseBody(body, resp))
            }
        }
    }

    private static String parseResponseBody(body, response) {
        if (emptyBody(body)) return "Response body is empty\n"

        if (jsonResponse(response)) return "Response body is: ${toJson(body)}\n"

        return body
    }

    private static boolean jsonResponse(response) {
        def contentTypeHeader = response.headers.find { it.name.equals('Content-Type') }

        JSON.toString().equals(contentTypeHeader.value)
    }

    private static boolean emptyBody(body) {
        !body || (body == null) || (body instanceof String && isBlank(body))
    }
}
