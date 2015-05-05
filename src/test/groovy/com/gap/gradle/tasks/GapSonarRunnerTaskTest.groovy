package com.gap.gradle.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import groovy.json.JsonSlurper
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito
import org.testng.Assert

import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class GapSonarRunnerTaskTest {

    def logger = LogFactory.getLog(GapSonarRunnerTaskTest)

    private Project project

    CommanderClient commanderClient

    GapSonarRunnerAuditorTask auditorTask

    ShellCommand mockShellCommand

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Before
    void setup() {

        project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build();

        project.apply plugin: 'sonar-runner'

        mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)

        commanderClient = new CommanderClient(mockShellCommand)

        auditorTask = new GapSonarRunnerAuditorTask(project, commanderClient)

        when(mockShellCommand.execute(['ectool', '--format', 'json', 'getProperties', '--path', '/projects/WM Segment Registry/ApplySonarRunner/'])).thenReturn(expectedJsonData.toString())
    }

    @Test
    void readPropertySheetTest(){
        def jsonSlurpedExpected = new JsonSlurper().parseText(expectedJsonData)

        def jsonSlurpedActual = auditorTask.readPropertySheet()
        Assert.assertEquals(jsonSlurpedActual, jsonSlurpedExpected, "Values does not match")
    }

    @Test
    void buildHtmlData(){
        def expectedHtml=new XmlSlurper().parseText(expectedHtmlData)

        def projectList = []
        def project1 = [:]
        project1.put("projectSegment", "projectSegment 1")
        project1.put("lastRun", "lastRun 1")
        def project2 = [:]
        project2.put("projectSegment", "projectSegment 2")
        project2.put("lastRun", "lastRun 2")
        projectList.add(project1)
        projectList.add(project2)

        def actualHtml = new XmlSlurper().parseText(auditorTask.buildHtmlData(projectList).toString())

        Assert.assertEquals(actualHtml, expectedHtml, "Values mismatch")
    }

    @Test
    void createOrUpdateContents(){
        auditorTask.createOrUpdateContents(expectedHtmlData, "/tmp/")
        File actualFile=new File("/tmp/",auditorTask.REPORT_FILE_NAME)
        Assert.assertEquals(actualFile.getText(), expectedHtmlData, "Values mismatch")
    }

    def expectedJsonData= """{
    "requestId": "1",
    "propertySheet": {
        "owner": "Fr7t8p5",
        "modifyTime": "2015-05-04T18:57:35.220Z",
        "propertySheetId": "0ac5a6f3-eea4-11e4-8100-00505625f614",
        "lastModifiedBy": "project: WM Gradle-1.46",
        "property": [
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-04-30T23:12:24.237Z",
                "propertyId": "5c690be0-ef8e-11e4-b898-00505625f614",
                "propertyName": "Assortment Service:Assortment Service:Component Segment",
                "modifyTime": "2015-05-04T11:15:09.030Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: Custom Package Facade Service",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T17:12:42.438Z",
                "propertyId": "c64d7cea-f280-11e4-9d9f-00505625f614",
                "propertyName": "Custom Package Facade Service:Custom Package Facade Service:Run Sonar",
                "modifyTime": "2015-05-04T17:12:42.438Z",
                "lastModifiedBy": "project: Custom Package Facade Service"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T18:54:35.900Z",
                "propertyId": "02365dd2-f28f-11e4-b910-00505625f614",
                "propertyName": "Dashboard Service:Dashboard Service:Component Segment",
                "modifyTime": "2015-05-04T18:54:35.900Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T18:55:22.838Z",
                "propertyId": "1e30d80a-f28f-11e4-b725-00505625f614",
                "propertyName": "Foundation Composite Authc Realm:Foundation Composite Authc Realm:Build Segment",
                "modifyTime": "2015-05-04T18:55:22.838Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T18:56:39.976Z",
                "propertyId": "4c29f291-f28f-11e4-8206-00505625f614",
                "propertyName": "Foundation Http HealthCheck:Foundation Http HealthCheck:Build Segment",
                "modifyTime": "2015-05-04T18:56:39.976Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T18:56:17.249Z",
                "propertyId": "3e9dee4e-f28f-11e4-b65d-00505625f614",
                "propertyName": "Foundation Ldap AuthzRealm:Foundation Ldap AuthzRealm:Build Segment",
                "modifyTime": "2015-05-04T18:56:17.249Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T18:57:20.533Z",
                "propertyId": "6456c2da-f28f-11e4-885d-00505625f614",
                "propertyName": "Foundation Mongo AuthzRealm:Foundation Mongo AuthzRealm:Build Segment",
                "modifyTime": "2015-05-04T18:57:20.533Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T18:57:35.220Z",
                "propertyId": "6d1894c3-f28f-11e4-88ed-00505625f614",
                "propertyName": "Foundation Mongo HealthCheck:Foundation Mongo HealthCheck:Build Segment",
                "modifyTime": "2015-05-04T18:57:35.220Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T18:56:36.699Z",
                "propertyId": "4a36add9-f28f-11e4-ae1d-00505625f614",
                "propertyName": "Foundation OpenToken AuthcRealm:Foundation OpenToken AuthcRealm:Build Segment",
                "modifyTime": "2015-05-04T18:56:36.699Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T18:55:34.043Z",
                "propertyId": "24ddfb23-f28f-11e4-b725-00505625f614",
                "propertyName": "Foundation Platform HealthCheck:Foundation Platform HealthCheck:Build Segment",
                "modifyTime": "2015-05-04T18:55:34.043Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-01",
                "expandable": "1",
                "createTime": "2015-04-30T22:21:36.591Z",
                "propertyId": "43de419a-ef87-11e4-8d64-00505625f614",
                "propertyName": "Plan - Global Assortment webapp:Plan - Global Assortment webapp:Component Segment",
                "modifyTime": "2015-05-01T19:31:52.100Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-04-30T22:21:30.791Z",
                "propertyId": "4068a28c-ef87-11e4-ab53-00505625f614",
                "propertyName": "Plan - Planning Service:Plan - Planning Service:Component Segment",
                "modifyTime": "2015-05-04T09:47:47.591Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T22:14:08.118Z",
                "propertyId": "62f84dd4-f04f-11e4-a335-00505625f614",
                "propertyName": "Plan Support:Plan Support:Component Segment",
                "modifyTime": "2015-05-04T18:04:57.255Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T22:41:01.155Z",
                "propertyId": "246d6c4c-f053-11e4-a474-00505625f614",
                "propertyName": "Platform-Catalog:Platform-Catalog:Component Segment",
                "modifyTime": "2015-05-04T19:17:07.905Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-01",
                "expandable": "1",
                "createTime": "2015-05-01T21:35:04.508Z",
                "propertyId": "ee16ae40-f049-11e4-ad77-00505625f614",
                "propertyName": "Platform-Harness:Platform-Harness:Sonar Segment",
                "modifyTime": "2015-05-01T21:35:04.508Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: Product Classification",
                "value": "2015-05-02",
                "expandable": "1",
                "createTime": "2015-05-02T17:36:30.530Z",
                "propertyId": "c4b9737c-f0f1-11e4-ac58-00505625f614",
                "propertyName": "Product Classification:Product Classification:Run Sonar",
                "modifyTime": "2015-05-02T17:36:30.530Z",
                "lastModifiedBy": "project: Product Classification"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-04-30T22:15:01.062Z",
                "propertyId": "58212277-ef86-11e4-b87e-00505625f614",
                "propertyName": "Product:Product:Component Segment",
                "modifyTime": "2015-05-04T13:46:16.618Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T07:17:42.821Z",
                "propertyId": "286ed4fb-efd2-11e4-94cc-00505625f614",
                "propertyName": "RI-Allocation:RI-Allocation:Component Segment",
                "modifyTime": "2015-05-04T07:18:20.400Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-01",
                "expandable": "1",
                "createTime": "2015-05-01T22:05:28.175Z",
                "propertyId": "2d10ab67-f04e-11e4-8625-00505625f614",
                "propertyName": "RMS:RMS:Component Segment",
                "modifyTime": "2015-05-01T22:05:28.175Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-04-30T22:20:45.156Z",
                "propertyId": "25360cbf-ef87-11e4-b898-00505625f614",
                "propertyName": "RSC-Foundation:RSC-Foundation:Component Segment",
                "modifyTime": "2015-05-04T16:33:34.906Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: Retail Import Order Service",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T00:25:15.281Z",
                "propertyId": "89c194c3-ef98-11e4-846a-00505625f614",
                "propertyName": "Retail Import Order Service:Retail Import Order Service:Run Sonar",
                "modifyTime": "2015-05-04T17:35:53.291Z",
                "lastModifiedBy": "project: Retail Import Order Service"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-04-30T23:31:56.344Z",
                "propertyId": "17090157-ef91-11e4-b1fc-00505625f614",
                "propertyName": "Runway:Runway:Component Segment",
                "modifyTime": "2015-05-04T17:30:48.248Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T17:39:55.381Z",
                "propertyId": "146005ca-f029-11e4-a30d-00505625f614",
                "propertyName": "SCMS ACL:SCMS ACL:Component Segment",
                "modifyTime": "2015-05-04T18:23:40.530Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T07:12:59.568Z",
                "propertyId": "7f9a00e3-efd1-11e4-94f1-00505625f614",
                "propertyName": "Shipment Service:Shipment Service:Component Segment",
                "modifyTime": "2015-05-04T07:10:50.288Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-04-30T20:48:27.342Z",
                "propertyId": "406b7452-ef7a-11e4-adc7-00505625f614",
                "propertyName": "SnapServe iOS:SnapServe iOS:Component Segment",
                "modifyTime": "2015-05-04T16:29:10.442Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T07:08:26.658Z",
                "propertyId": "dcef16ef-efd0-11e4-8625-00505625f614",
                "propertyName": "SnapServe-BarcodeGenerator:SnapServe-BarcodeGenerator:Component Segment",
                "modifyTime": "2015-05-04T07:06:55.808Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-01",
                "expandable": "1",
                "createTime": "2015-05-01T19:17:06.881Z",
                "propertyId": "a83a4a86-f036-11e4-a335-00505625f614",
                "propertyName": "SnapServe-ElectronicJournalService:SnapServe-ElectronicJournalService:Component Segment",
                "modifyTime": "2015-05-01T19:17:06.881Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T18:05:44.575Z",
                "propertyId": "2f018449-f288-11e4-8385-00505625f614",
                "propertyName": "SnapServe-SnapCard UI Pipeline:SnapServe-SnapCard UI Pipeline:Component Segment",
                "modifyTime": "2015-05-04T18:05:44.575Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-01",
                "expandable": "1",
                "createTime": "2015-05-01T20:20:48.570Z",
                "propertyId": "8e1f505a-f03f-11e4-861e-00505625f614",
                "propertyName": "SnapServe-SnapScan:SnapServe-SnapScan:Component Segment",
                "modifyTime": "2015-05-01T20:20:48.570Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-04-30T22:41:53.302Z",
                "propertyId": "191550ab-ef8a-11e4-9703-00505625f614",
                "propertyName": "SnapServe-SnapSell:SnapServe-SnapSell:Component Segment",
                "modifyTime": "2015-05-04T15:47:07.436Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T17:39:28.226Z",
                "propertyId": "04307fb9-f029-11e4-aa62-00505625f614",
                "propertyName": "SnapServe-TaxService:SnapServe-TaxService:Tax Component Segment",
                "modifyTime": "2015-05-04T18:43:20.677Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T20:19:09.874Z",
                "propertyId": "534ba665-f03f-11e4-beff-00505625f614",
                "propertyName": "SnapServe-TenderAuthorizations:SnapServe-TenderAuthorizations:Tender Component Segment",
                "modifyTime": "2015-05-04T17:56:43.099Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-01",
                "expandable": "1",
                "createTime": "2015-04-30T22:48:48.688Z",
                "propertyId": "10accc83-ef8b-11e4-8a2a-00505625f614",
                "propertyName": "SnapServe-TransactionService:SnapServe-TransactionService:Component Segment",
                "modifyTime": "2015-05-01T17:17:22.108Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T08:03:49.615Z",
                "propertyId": "9991f06c-efd8-11e4-ad77-00505625f614",
                "propertyName": "SnapServe-snapinvoice:SnapServe-snapinvoice:Component Segment",
                "modifyTime": "2015-05-04T18:28:36.519Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T16:24:11.170Z",
                "propertyId": "7fd10fbd-f01e-11e4-b03b-00505625f614",
                "propertyName": "Store Service:Store Service:Component Segment",
                "modifyTime": "2015-05-04T16:35:03.235Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "Fr7t8p5",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-04T16:33:04.518Z",
                "propertyId": "3cf55e6b-f27b-11e4-90a7-00505625f614",
                "propertyName": "TestProjectName:TestSegment",
                "modifyTime": "2015-05-04T16:33:04.518Z",
                "lastModifiedBy": "Fr7t8p5"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-05-01T07:17:32.516Z",
                "propertyId": "224a1b2d-efd2-11e4-bc58-00505625f614",
                "propertyName": "UserAuthenticationService:UserAuthenticationService:Component Segment",
                "modifyTime": "2015-05-04T07:26:02.357Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Exec-1.24",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-04-30T15:21:55.533Z",
                "propertyId": "a2c9e266-ef4c-11e4-b857-00505625f614",
                "propertyName": "Watchmen Framework:Watchmen Framework:Component Segment",
                "modifyTime": "2015-05-04T15:30:45.942Z",
                "lastModifiedBy": "project: WM Exec-1.24"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-03",
                "expandable": "1",
                "createTime": "2015-05-02T10:49:59.332Z",
                "propertyId": "fa674d46-f0b8-11e4-89aa-00505625f614",
                "propertyName": "Watchmen Harness Service:Watchmen Harness Service:Component Segment",
                "modifyTime": "2015-05-03T14:15:15.587Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            },
            {
                "owner": "project: WM Exec-1.24",
                "value": "2015-05-03",
                "expandable": "1",
                "createTime": "2015-05-02T10:41:44.081Z",
                "propertyId": "d3358a1e-f0b7-11e4-8fee-00505625f614",
                "propertyName": "Watchmen Reference Application:Watchmen Reference Application:Component Segment",
                "modifyTime": "2015-05-03T14:17:42.533Z",
                "lastModifiedBy": "project: WM Exec-1.24"
            },
            {
                "owner": "project: WM Gradle-1.46",
                "value": "2015-05-04",
                "expandable": "1",
                "createTime": "2015-04-30T22:32:35.523Z",
                "propertyId": "cc9fe912-ef88-11e4-8ed3-00505625f614",
                "propertyName": "mobile-emv:mobile-emv:Component Segment",
                "modifyTime": "2015-05-04T18:51:40.770Z",
                "lastModifiedBy": "project: WM Gradle-1.46"
            }
        ],
        "createTime": "2015-04-29T19:15:05.057Z"
    }
}"""

    def expectedHtmlData = """<html>
  <head>
    <title>Auditor For Gap Sonar Runner</title>
  </head>
  <body>
    <h1>Auditor For Gap Sonar Runner</h1>
    <table>
      <tr>
        <td projectSegment='projectSegment 1' lastRun='lastRun 1'>
          <td>projectSegment 1</td>
          <td>lastRun 1</td>
        </td>
      </tr>
      <tr>
        <td projectSegment='projectSegment 2' lastRun='lastRun 2'>
          <td>projectSegment 2</td>
          <td>lastRun 2</td>
        </td>
      </tr>
    </table>
  </body>
</html>"""
}
