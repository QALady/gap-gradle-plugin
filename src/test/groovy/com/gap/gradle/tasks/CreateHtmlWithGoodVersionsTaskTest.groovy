package com.gap.gradle.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.SegmentRegistry
import com.gap.pipeline.utils.EnvironmentStub
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito

import java.text.ParseException
import java.text.SimpleDateFormat

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class CreateHtmlWithGoodVersionsTaskTest {
    def logger = LogFactory.getLog(CreateHtmlWithGoodVersionsTaskTest)

    //private def segmentIdentifier = "Test Project:Test Procedure"

    private Project project

    private EnvironmentStub environmentStub

    private CommanderClient commanderClient

    private ShellCommand mockShellCommand

    private SegmentRegistry segmentRegistry

    private CreateHtmlWithGoodVersionsTask task


    @Rule
    public  TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Before
    void setup() {

        project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build();

        project.apply plugin: 'gap-wm-manual'

        def ivyDependenciesOut = "com.gap.watchmen.diamondDependency.iso.diamondDependencyC:ci\n" +
                "com.gap.watchmen.diamondDependency.iso.diamondDependencyB:ci"

        environmentStub = new EnvironmentStub();

        mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)

        when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/ivyDependencies'])).thenReturn(ivyDependenciesOut)

        when(mockShellCommand.execute(['ectool', 'getProperty', "/server/watchmen_config/sharedHtdocs"])).thenReturn("/tmp/test/")
        //when(mockShellCommand.execute(['ectool', 'getProperty', "/server/watchmen_config/sharedHtdocs"])).thenReturn(temporaryFolder)

        commanderClient = new CommanderClient(mockShellCommand, environmentStub)

        segmentRegistry = new SegmentRegistry(commanderClient)

        project.segmentIdentifier = "WM Diamond Dependency:ISOC Segment"

        task = new CreateHtmlWithGoodVersionsTask(project, commanderClient, segmentRegistry)

    }

    @Test
    void shouldGetIvyDependencies() {
        String[] expectedIvyDependencies = ["com.gap.watchmen.diamondDependency.iso.diamondDependencyC:ci", "com.gap.watchmen.diamondDependency.iso.diamondDependencyB:ci"]
        def actualIvyDependencies = task.getIvyDependencies()

        assertEquals(expectedIvyDependencies, actualIvyDependencies)
    }

    @Test
    void shouldBuildDependenciesHtml() {

        mockForBuildDependenciesHtml()

        def actualDependenciesHtml = task.buildDependenciesHtml()

        logger.info("actualDependenciesHtml : " + actualDependenciesHtml)

        def lastIndex = 2934

        assertEquals("Las index of element </div> must be $lastIndex", actualDependenciesHtml.lastIndexOf("</div>"), lastIndex)

    }

    @Test
    void shouldGenerateTimeWithFormat() {
        String generatedTime = task.generateTimeWithFormat()

        String generatedTimeFirst12Characters = generatedTime.substring(0, 12)

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMddyyyy")
        sdf1.setLenient(true)

        try {
            logger.info("Parsing $generatedTimeFirst12Characters")
            sdf1.parse(generatedTimeFirst12Characters)
            assertEquals(generatedTimeFirst12Characters.length(), 12)
        }
        catch (ParseException pe) {
            logger.error("Parse Failed")
            throw pe
        }
    }

    @Test
    void shouldWriteHtmlPage() {

        mockForBuildDependenciesHtml()

        File htmlFile = task.createHtmlFile()

        def dependenciesHtml = task.buildDependenciesHtml()

        def htmlContent = task.buildHtmlPage(dependenciesHtml)

        task.writeToFile(htmlFile, htmlContent)

        assertTrue(htmlFile.exists())
        assertTrue(htmlFile.length() > 0)
        //todo must check contents
    }


    @Test
    void shouldWriteManualSegmentJS() {
        File jsFile = task.writeJSFile()

        assertTrue(jsFile.exists())
        assertTrue(jsFile.length() > 0)
        //todo must check contents
    }

    @Test
    void shouldWriteCSS() {
        File cssFile = task.writeCSSFile()

        assertTrue(cssFile.exists())
        assertTrue(cssFile.length() > 0)
        //todo must check contents
    }


    @Test
    void shouldCleanSpacesAndColons() {

        String fileName1 = " testing: testing "
        String expectedFileName1 = "_testing__testing_"
        String actualFileName1 = task.cleanSpacesAndColons(fileName1)
        assertEquals(expectedFileName1, actualFileName1)

        String fileName2 = " testing: testing :::  kdkdk:_  "
        String expectedFileName2 = "_testing__testing______kdkdk____"
        String actualFileName2 = task.cleanSpacesAndColons(fileName2)

        assertEquals(expectedFileName2, actualFileName2)
    }


    private void mockForBuildDependenciesHtml() {
        when(mockShellCommand.execute(['ectool', 'getProperty', "/projects[WM Segment Registry]/IdentifierRegistry/com.gap.watchmen.diamondDependency.iso.diamondDependencyC:ci/segment"])).thenReturn("WM Diamond Dependency:ISOC Segment")
        when(mockShellCommand.execute(['ectool', 'getProperties', "--path", "/projects[WM Segment Registry]/SegmentRegistry/WM Diamond Dependency:ISOC Segment/goodVersions", '--resurce', '-1'])).thenReturn(result01)

//        def coberturaString = '{"propertySheet":[{"property":{"propertyName":"1.1"}}, {"property":{"propertyName":"2.2"}}]}'
//
//        when(mockShellCommand.execute(['ectool', '--format', 'json', 'getProperties', '--key', "WM Diamond Dependency:ISOC Segment"])).thenReturn(coberturaString)
//        when(mockShellCommand.execute(['ectool', 'getProperty', '/projects[WM Segment Registry]/SegmentRegistry/com.gap.watchmen.diamondDependency.iso.diamondDependencyC:ci/goodVersions/2.2/resolvedDependencies'])).thenReturn("com.gap.watchmen.diamondDependency:diamondDependencyC")
//
//        when(mockShellCommand.execute(['ectool', 'getProperty', "/projects[WM Segment Registry]/IdentifierRegistry/org.codehaus.groovy:groovy-all/segment"])).thenReturn("org.codehaus.groovy:groovy-all")
        when(mockShellCommand.execute(['ectool', 'getProperty', "/projects[WM Segment Registry]/SegmentRegistry/WM Diamond Dependency:ISOC Segment/goodVersions/propertySheetId"])).thenReturn("0537876e-0de3-11e4-8189-00505625f614")
        when(mockShellCommand.execute(['ectool', '--format', 'json', 'getProperties', '--propertySheetId', '0537876e-0de3-11e4-8189-00505625f614'])).thenReturn(json01)

        when(mockShellCommand.execute(['ectool', 'getProperty', '/projects[WM Segment Registry]/IdentifierRegistry/com.gap.watchmen.diamondDependency.iso.diamondDependencyB/goodVersions/propertySheetId'])).thenReturn()
//
//        def groovyAllString = '{"propertySheet":[{"property":{"propertyName":"1.12"}}, {"property":{"propertyName":"2.14"}}]}'
//
//        when(mockShellCommand.execute(['ectool', '--format', 'json', 'getProperties', '--key', "groovy-all"])).thenReturn(groovyAllString)
//        when(mockShellCommand.execute(['ectool', 'getProperty', '/projects[WM Segment Registry]/SegmentRegistry/org.codehaus.groovy:groovy-all/goodVersions/2.14/resolvedDependencies'])).thenReturn("com.gap.watchmen.diamondDependency:diamondDependencyB")
    }


    def result01="""<response requestId="1" nodeId="10.105.68.77">
    <propertySheet>
      <propertySheetId>0537876e-0de3-11e4-8189-00505625f614</propertySheetId>
      <createTime>2014-07-17T18:49:02.288Z</createTime>
      <lastModifiedBy>project: WM Segment-2.2</lastModifiedBy>
      <modifyTime>2014-10-04T03:33:10.819Z</modifyTime>
      <owner>project: WM Segment-1.90</owner>
      <property>
        <propertyId>73789fea-193a-11e4-86d4-00505625f614</propertyId>
        <propertyName>2413</propertyName>
        <createTime>2014-08-01T05:12:36.189Z</createTime>
        <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
        <modifyTime>2014-08-01T05:12:36.189Z</modifyTime>
        <owner>project: WM Segment-1.95</owner>
        <propertySheet>
          <propertySheetId>73789feb-193a-11e4-86d4-00505625f614</propertySheetId>
          <createTime>2014-08-01T05:12:36.189Z</createTime>
          <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
          <modifyTime>2014-08-01T05:12:37.556Z</modifyTime>
          <owner>project: WM Segment-1.95</owner>
          <property>
            <propertyId>7413a6c8-193a-11e4-8796-00505625f614</propertyId>
            <propertyName>resolvedDependencies</propertyName>
            <createTime>2014-08-01T05:12:37.105Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-01T05:12:37.105Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>
------------------------------------------------------------
Root project
------------------------------------------------------------

archives - Configuration for archive artifacts.
--- com.gap.watchmen.diamondDependency:diamondDependencyC:+ -> 33

default - Configuration for default artifacts.
No dependencies

pipeline
No dependencies</value>
          </property>
          <property>
            <propertyId>74612a83-193a-11e4-8c78-00505625f614</propertyId>
            <propertyName>scmRevision</propertyName>
            <createTime>2014-08-01T05:12:37.556Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-01T05:12:37.556Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>2445</value>
          </property>
          <property>
            <propertyId>73c20453-193a-11e4-b80a-00505625f614</propertyId>
            <propertyName>time</propertyName>
            <createTime>2014-08-01T05:12:36.480Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-01T05:12:36.480Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>1406869955</value>
          </property>
        </propertySheet>
      </property>
      <property>
        <propertyId>93481e08-19a6-11e4-9e8b-00505625f614</propertyId>
        <propertyName>2414</propertyName>
        <createTime>2014-08-01T18:06:35.275Z</createTime>
        <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
        <modifyTime>2014-08-01T18:06:35.275Z</modifyTime>
        <owner>project: WM Segment-1.95</owner>
        <propertySheet>
          <propertySheetId>93484519-19a6-11e4-9e8b-00505625f614</propertySheetId>
          <createTime>2014-08-01T18:06:35.275Z</createTime>
          <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
          <modifyTime>2014-08-01T18:06:36.506Z</modifyTime>
          <owner>project: WM Segment-1.95</owner>
          <property>
            <propertyId>93da7340-19a6-11e4-afbf-00505625f614</propertyId>
            <propertyName>resolvedDependencies</propertyName>
            <createTime>2014-08-01T18:06:36.059Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-01T18:06:36.059Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>
------------------------------------------------------------
Root project
------------------------------------------------------------

archives - Configuration for archive artifacts.
--- com.gap.watchmen.diamondDependency:diamondDependencyC:+ -> 34

default - Configuration for default artifacts.
No dependencies

pipeline
No dependencies</value>
          </property>
          <property>
            <propertyId>94186637-19a6-11e4-a348-00505625f614</propertyId>
            <propertyName>scmRevision</propertyName>
            <createTime>2014-08-01T18:06:36.506Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-01T18:06:36.506Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>2445</value>
          </property>
          <property>
            <propertyId>938cef76-19a6-11e4-9835-00505625f614</propertyId>
            <propertyName>time</propertyName>
            <createTime>2014-08-01T18:06:35.480Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-01T18:06:35.480Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>1406916394</value>
          </property>
        </propertySheet>
      </property>
      <property>
        <propertyId>0dcae74d-1a04-11e4-a348-00505625f614</propertyId>
        <propertyName>2415</propertyName>
        <createTime>2014-08-02T05:15:43.965Z</createTime>
        <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
        <modifyTime>2014-08-02T05:15:43.965Z</modifyTime>
        <owner>project: WM Segment-1.95</owner>
        <propertySheet>
          <propertySheetId>0dcae74e-1a04-11e4-a348-00505625f614</propertySheetId>
          <createTime>2014-08-02T05:15:43.965Z</createTime>
          <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
          <modifyTime>2014-08-02T05:15:45.096Z</modifyTime>
          <owner>project: WM Segment-1.95</owner>
          <property>
            <propertyId>0e4239f2-1a04-11e4-a928-00505625f614</propertyId>
            <propertyName>resolvedDependencies</propertyName>
            <createTime>2014-08-02T05:15:44.589Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-02T05:15:44.589Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>
------------------------------------------------------------
Root project
------------------------------------------------------------

archives - Configuration for archive artifacts.
--- com.gap.watchmen.diamondDependency:diamondDependencyC:+ -> 35

default - Configuration for default artifacts.
No dependencies

pipeline
No dependencies</value>
          </property>
          <property>
            <propertyId>0e8adc10-1a04-11e4-9757-00505625f614</propertyId>
            <propertyName>scmRevision</propertyName>
            <createTime>2014-08-02T05:15:45.096Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-02T05:15:45.096Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>2445</value>
          </property>
          <property>
            <propertyId>0df8ae2b-1a04-11e4-a3a6-00505625f614</propertyId>
            <propertyName>time</propertyName>
            <createTime>2014-08-02T05:15:44.214Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-02T05:15:44.214Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>1406956543</value>
          </property>
        </propertySheet>
      </property>
      <property>
        <propertyId>eb5d544a-1acc-11e4-bcfa-00505625f614</propertyId>
        <propertyName>2416</propertyName>
        <createTime>2014-08-03T05:13:35.143Z</createTime>
        <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
        <modifyTime>2014-08-03T05:13:35.143Z</modifyTime>
        <owner>project: WM Segment-1.95</owner>
        <propertySheet>
          <propertySheetId>eb5d544b-1acc-11e4-bcfa-00505625f614</propertySheetId>
          <createTime>2014-08-03T05:13:35.143Z</createTime>
          <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
          <modifyTime>2014-08-03T05:13:36.003Z</modifyTime>
          <owner>project: WM Segment-1.95</owner>
          <property>
            <propertyId>ebbefbf9-1acc-11e4-9757-00505625f614</propertyId>
            <propertyName>resolvedDependencies</propertyName>
            <createTime>2014-08-03T05:13:35.629Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-03T05:13:35.629Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>
------------------------------------------------------------
Root project
------------------------------------------------------------

archives - Configuration for archive artifacts.
--- com.gap.watchmen.diamondDependency:diamondDependencyC:+ -> 36

default - Configuration for default artifacts.
No dependencies

pipeline
No dependencies</value>
          </property>
          <property>
            <propertyId>ebfca144-1acc-11e4-9210-00505625f614</propertyId>
            <propertyName>scmRevision</propertyName>
            <createTime>2014-08-03T05:13:36.003Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-03T05:13:36.003Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>2445</value>
          </property>
          <property>
            <propertyId>eb891e63-1acc-11e4-9bd1-00505625f614</propertyId>
            <propertyName>time</propertyName>
            <createTime>2014-08-03T05:13:35.283Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-03T05:13:35.283Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>1407042814</value>
          </property>
        </propertySheet>
      </property>
      <property>
        <propertyId>6a4be2d3-1b96-11e4-8189-00505625f614</propertyId>
        <propertyName>2417</propertyName>
        <createTime>2014-08-04T05:15:56.899Z</createTime>
        <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
        <modifyTime>2014-08-04T05:15:56.899Z</modifyTime>
        <owner>project: WM Segment-1.95</owner>
        <propertySheet>
          <propertySheetId>6a4be2d4-1b96-11e4-8189-00505625f614</propertySheetId>
          <createTime>2014-08-04T05:15:56.899Z</createTime>
          <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
          <modifyTime>2014-08-04T05:15:58.148Z</modifyTime>
          <owner>project: WM Segment-1.95</owner>
          <property>
            <propertyId>6ad47419-1b96-11e4-8796-00505625f614</propertyId>
            <propertyName>resolvedDependencies</propertyName>
            <createTime>2014-08-04T05:15:57.637Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-04T05:15:57.637Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>
------------------------------------------------------------
Root project
------------------------------------------------------------

archives - Configuration for archive artifacts.
--- com.gap.watchmen.diamondDependency:diamondDependencyC:+ -> 37

default - Configuration for default artifacts.
No dependencies

pipeline
No dependencies</value>
          </property>
          <property>
            <propertyId>6b2998ce-1b96-11e4-a592-00505625f614</propertyId>
            <propertyName>scmRevision</propertyName>
            <createTime>2014-08-04T05:15:58.148Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-04T05:15:58.148Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>2445</value>
          </property>
          <property>
            <propertyId>6a8e1c16-1b96-11e4-9210-00505625f614</propertyId>
            <propertyName>time</propertyName>
            <createTime>2014-08-04T05:15:57.176Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.95</lastModifiedBy>
            <modifyTime>2014-08-04T05:15:57.176Z</modifyTime>
            <owner>project: WM Segment-1.95</owner>
            <value>1407129356</value>
          </property>
        </propertySheet>
      </property>
      <property>
        <propertyId>db296db1-1d28-11e4-8ee0-00505625f614</propertyId>
        <propertyName>2418</propertyName>
        <createTime>2014-08-06T05:16:43.917Z</createTime>
        <lastModifiedBy>project: WM Segment-1.96</lastModifiedBy>
        <modifyTime>2014-08-06T05:16:43.917Z</modifyTime>
        <owner>project: WM Segment-1.96</owner>
        <propertySheet>
          <propertySheetId>db296db2-1d28-11e4-8ee0-00505625f614</propertySheetId>
          <createTime>2014-08-06T05:16:43.917Z</createTime>
          <lastModifiedBy>project: WM Segment-1.96</lastModifiedBy>
          <modifyTime>2014-08-06T05:16:45.237Z</modifyTime>
          <owner>project: WM Segment-1.96</owner>
          <property>
            <propertyId>dbbcfa72-1d28-11e4-8c78-00505625f614</propertyId>
            <propertyName>resolvedDependencies</propertyName>
            <createTime>2014-08-06T05:16:44.734Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.96</lastModifiedBy>
            <modifyTime>2014-08-06T05:16:44.734Z</modifyTime>
            <owner>project: WM Segment-1.96</owner>
            <value>
------------------------------------------------------------
Root project
------------------------------------------------------------

archives - Configuration for archive artifacts.
--- com.gap.watchmen.diamondDependency:diamondDependencyC:+ -> 39

default - Configuration for default artifacts.
No dependencies

pipeline
No dependencies</value>
          </property>
          <property>
            <propertyId>dc0c533b-1d28-11e4-b04d-00505625f614</propertyId>
            <propertyName>scmRevision</propertyName>
            <createTime>2014-08-06T05:16:45.237Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.96</lastModifiedBy>
            <modifyTime>2014-08-06T05:16:45.237Z</modifyTime>
            <owner>project: WM Segment-1.96</owner>
            <value>2446</value>
          </property>
          <property>
            <propertyId>db6f5074-1d28-11e4-8189-00505625f614</propertyId>
            <propertyName>time</propertyName>
            <createTime>2014-08-06T05:16:44.179Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.96</lastModifiedBy>
            <modifyTime>2014-08-06T05:16:44.179Z</modifyTime>
            <owner>project: WM Segment-1.96</owner>
            <value>1407302203</value>
          </property>
        </propertySheet>
      </property>
      <property>
        <propertyId>b9730c47-1e4c-11e4-bcd3-00505625f614</propertyId>
        <propertyName>2419</propertyName>
        <createTime>2014-08-07T16:06:00.489Z</createTime>
        <lastModifiedBy>project: WM Segment-1.96</lastModifiedBy>
        <modifyTime>2014-08-07T16:06:00.489Z</modifyTime>
        <owner>project: WM Segment-1.96</owner>
        <propertySheet>
          <propertySheetId>b9730c48-1e4c-11e4-bcd3-00505625f614</propertySheetId>
          <createTime>2014-08-07T16:06:00.489Z</createTime>
          <lastModifiedBy>project: WM Segment-1.96</lastModifiedBy>
          <modifyTime>2014-08-07T16:06:01.002Z</modifyTime>
          <owner>project: WM Segment-1.96</owner>
          <property>
            <propertyId>b9a9faca-1e4c-11e4-bcd3-00505625f614</propertyId>
            <propertyName>resolvedDependencies</propertyName>
            <createTime>2014-08-07T16:06:00.812Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.96</lastModifiedBy>
            <modifyTime>2014-08-07T16:06:00.812Z</modifyTime>
            <owner>project: WM Segment-1.96</owner>
            <value>
------------------------------------------------------------
Root project
------------------------------------------------------------

archives - Configuration for archive artifacts.
--- com.gap.watchmen.diamondDependency:diamondDependencyC:+ -> 40

default - Configuration for default artifacts.
No dependencies

pipeline
No dependencies</value>
          </property>
          <property>
            <propertyId>b9c68432-1e4c-11e4-8822-00505625f614</propertyId>
            <propertyName>scmRevision</propertyName>
            <createTime>2014-08-07T16:06:01.002Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.96</lastModifiedBy>
            <modifyTime>2014-08-07T16:06:01.002Z</modifyTime>
            <owner>project: WM Segment-1.96</owner>
            <value>2448</value>
          </property>
          <property>
            <propertyId>b9927b8d-1e4c-11e4-981f-00505625f614</propertyId>
            <propertyName>time</propertyName>
            <createTime>2014-08-07T16:06:00.640Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-1.96</lastModifiedBy>
            <modifyTime>2014-08-07T16:06:00.640Z</modifyTime>
            <owner>project: WM Segment-1.96</owner>
            <value>1407427560</value>
          </property>
        </propertySheet>
      </property>
      <property>
        <propertyId>2a2e3466-4b77-11e4-b7f0-00505625f614</propertyId>
        <propertyName>2420</propertyName>
        <createTime>2014-10-04T03:33:10.819Z</createTime>
        <lastModifiedBy>project: WM Segment-2.2</lastModifiedBy>
        <modifyTime>2014-10-04T03:33:10.819Z</modifyTime>
        <owner>project: WM Segment-2.2</owner>
        <propertySheet>
          <propertySheetId>2a2e3467-4b77-11e4-b7f0-00505625f614</propertySheetId>
          <createTime>2014-10-04T03:33:10.819Z</createTime>
          <lastModifiedBy>project: WM Segment-2.2</lastModifiedBy>
          <modifyTime>2014-10-04T03:33:11.223Z</modifyTime>
          <owner>project: WM Segment-2.2</owner>
          <property>
            <propertyId>2a5ac2d2-4b77-11e4-aafd-00505625f614</propertyId>
            <propertyName>resolvedDependencies</propertyName>
            <createTime>2014-10-04T03:33:11.091Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-2.2</lastModifiedBy>
            <modifyTime>2014-10-04T03:33:11.091Z</modifyTime>
            <owner>project: WM Segment-2.2</owner>
            <value>
------------------------------------------------------------
Root project
------------------------------------------------------------

archives - Configuration for archive artifacts.
--- com.gap.watchmen.diamondDependency:diamondDependencyC:+ -> 41

default - Configuration for default artifacts.
No dependencies

pipeline
No dependencies</value>
          </property>
          <property>
            <propertyId>2a6fd135-4b77-11e4-a5f9-00505625f614</propertyId>
            <propertyName>scmRevision</propertyName>
            <createTime>2014-10-04T03:33:11.223Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-2.2</lastModifiedBy>
            <modifyTime>2014-10-04T03:33:11.223Z</modifyTime>
            <owner>project: WM Segment-2.2</owner>
            <value>2586</value>
          </property>
          <property>
            <propertyId>2a48733f-4b77-11e4-85ba-00505625f614</propertyId>
            <propertyName>time</propertyName>
            <createTime>2014-10-04T03:33:10.960Z</createTime>
            <expandable>1</expandable>
            <lastModifiedBy>project: WM Segment-2.2</lastModifiedBy>
            <modifyTime>2014-10-04T03:33:10.960Z</modifyTime>
            <owner>project: WM Segment-2.2</owner>
            <value>1412393590</value>
          </property>
        </propertySheet>
      </property>
    </propertySheet>
  </response>"""

    def json01="""{
   "requestId" : "1",
   "propertySheet" : {
      "owner" : "project: WM Segment-1.90",
      "modifyTime" : "2014-10-04T03:33:10.819Z",
      "propertySheetId" : "0537876e-0de3-11e4-8189-00505625f614",
      "lastModifiedBy" : "project: WM Segment-2.2",
      "property" : [
         {
            "owner" : "project: WM Segment-1.95",
            "propertyId" : "73789fea-193a-11e4-86d4-00505625f614",
            "propertyName" : "2413",
            "propertySheetId" : "73789feb-193a-11e4-86d4-00505625f614",
            "modifyTime" : "2014-08-01T05:12:36.189Z",
            "lastModifiedBy" : "project: WM Segment-1.95",
            "createTime" : "2014-08-01T05:12:36.189Z"
         },
         {
            "owner" : "project: WM Segment-1.95",
            "propertyId" : "93481e08-19a6-11e4-9e8b-00505625f614",
            "propertyName" : "2414",
            "propertySheetId" : "93484519-19a6-11e4-9e8b-00505625f614",
            "modifyTime" : "2014-08-01T18:06:35.275Z",
            "lastModifiedBy" : "project: WM Segment-1.95",
            "createTime" : "2014-08-01T18:06:35.275Z"
         },
         {
            "owner" : "project: WM Segment-1.95",
            "propertyId" : "0dcae74d-1a04-11e4-a348-00505625f614",
            "propertyName" : "2415",
            "propertySheetId" : "0dcae74e-1a04-11e4-a348-00505625f614",
            "modifyTime" : "2014-08-02T05:15:43.965Z",
            "lastModifiedBy" : "project: WM Segment-1.95",
            "createTime" : "2014-08-02T05:15:43.965Z"
         },
         {
            "owner" : "project: WM Segment-1.95",
            "propertyId" : "eb5d544a-1acc-11e4-bcfa-00505625f614",
            "propertyName" : "2416",
            "propertySheetId" : "eb5d544b-1acc-11e4-bcfa-00505625f614",
            "modifyTime" : "2014-08-03T05:13:35.143Z",
            "lastModifiedBy" : "project: WM Segment-1.95",
            "createTime" : "2014-08-03T05:13:35.143Z"
         },
         {
            "owner" : "project: WM Segment-1.95",
            "propertyId" : "6a4be2d3-1b96-11e4-8189-00505625f614",
            "propertyName" : "2417",
            "propertySheetId" : "6a4be2d4-1b96-11e4-8189-00505625f614",
            "modifyTime" : "2014-08-04T05:15:56.899Z",
            "lastModifiedBy" : "project: WM Segment-1.95",
            "createTime" : "2014-08-04T05:15:56.899Z"
         },
         {
            "owner" : "project: WM Segment-1.96",
            "propertyId" : "db296db1-1d28-11e4-8ee0-00505625f614",
            "propertyName" : "2418",
            "propertySheetId" : "db296db2-1d28-11e4-8ee0-00505625f614",
            "modifyTime" : "2014-08-06T05:16:43.917Z",
            "lastModifiedBy" : "project: WM Segment-1.96",
            "createTime" : "2014-08-06T05:16:43.917Z"
         },
         {
            "owner" : "project: WM Segment-1.96",
            "propertyId" : "b9730c47-1e4c-11e4-bcd3-00505625f614",
            "propertyName" : "2419",
            "propertySheetId" : "b9730c48-1e4c-11e4-bcd3-00505625f614",
            "modifyTime" : "2014-08-07T16:06:00.489Z",
            "lastModifiedBy" : "project: WM Segment-1.96",
            "createTime" : "2014-08-07T16:06:00.489Z"
         },
         {
            "owner" : "project: WM Segment-2.2",
            "propertyId" : "2a2e3466-4b77-11e4-b7f0-00505625f614",
            "propertyName" : "2420",
            "propertySheetId" : "2a2e3467-4b77-11e4-b7f0-00505625f614",
            "modifyTime" : "2014-10-04T03:33:10.819Z",
            "lastModifiedBy" : "project: WM Segment-2.2",
            "createTime" : "2014-10-04T03:33:10.819Z"
         }
      ],
      "createTime" : "2014-07-17T18:49:02.288Z"
   }
}"""
}
