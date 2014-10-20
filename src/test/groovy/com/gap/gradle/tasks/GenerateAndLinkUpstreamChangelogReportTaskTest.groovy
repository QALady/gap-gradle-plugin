package com.gap.gradle.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.gradle.utils.ShellCommandException
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.utils.EnvironmentStub
import groovy.xml.MarkupBuilder
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito

import static org.junit.Assert.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class GenerateAndLinkUpstreamChangelogReportTaskTest {

	Project project
	GenerateAndLinkUpstreamChangelogReportTask task

	private EnvironmentStub environmentStub
	private ShellCommand mockShellCommand
	final JOB_ID = "1345555"
	def commander

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder()

	@Rule
	public ExpectedException thrown = ExpectedException.none();


	@Before
	void setUp() {
		project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build();
		project.apply plugin: 'gappipeline'

		environmentStub = new EnvironmentStub();
		environmentStub.setValue('COMMANDER_JOBID', JOB_ID)
		environmentStub.setValue('COMMANDER_WORKSPACE_UNIX',"/tmp/")
		mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)

		commander = new CommanderClient(mockShellCommand, environmentStub)

		task = new GenerateAndLinkUpstreamChangelogReportTask(project)
		task.shellCommand = mockShellCommand
		task.commanderClient = commander
		task.thisJobId = commander.getJobId()
	}


	@Test
	void shouldProcessUpstreamChangeLog() {

		final TEST_UPSTREAM_JOB_ID = "55555"
		final TEST_UPSTREAM_JOB_VALUE = "val/val2/val3/$TEST_UPSTREAM_JOB_ID"

		when(mockShellCommand.execute(['ectool', 'getProperty', "/jobs[1345555]/projectName"])).thenReturn("Project_Name")
		when(mockShellCommand.execute(['ectool', 'getProperty', "/jobs[1345555]/liveProcedure"])).thenReturn("liveProcedure")
		when(mockShellCommand.execute(['ectool', 'getProperties', '--path', "/jobs[1345555]/ecscm_changeLogs", '--recurse', '1'])).thenReturn(getECPropertySheetResp())
		when(mockShellCommand.execute(['ectool', 'getProperty', '/jobs[1345555]/report-urls/Upstream Job'])).thenReturn(TEST_UPSTREAM_JOB_VALUE)
		when(mockShellCommand.execute(['ectool', 'getProperties', '--path', "/jobs[55555]/ecscm_changeLogs", '--recurse', '1'])).thenReturn(getECPropertySheetResp())
		when(mockShellCommand.execute(['ectool', 'getProperty', '/jobs[55555]/report-urls/Upstream Job'])).thenReturn("invalid")

		def actualFile= new File(task.upstream_changelog_file)

		assertTrue(actualFile.size()==0)

		task.processUpstreamChangeLog()

		assertTrue(actualFile.size()>0)
	}

	@Test(expected = Exception.class)
	void shouldExecuteWithException() {
		when(mockShellCommand.execute(['ectool', 'getProperty', "/jobs[1345555]/projectName"])).thenThrow(new Exception("Generated Exception"))
		task.execute()
	}

	@Test
	void shouldGetUpstreamJobIdHappyPathReturnUpstreamJobIdFromTokenizedReportValue() {

		final TEST_UPSTREAM_JOB_ID = "4567890"
		final TEST_UPSTREAM_JOB_VALUE = "val/val2/val3/$TEST_UPSTREAM_JOB_ID"
		final TEST_JOB_ID = "123456"
		when(mockShellCommand.execute(['ectool', 'getProperty', '/jobs[123456]/report-urls/Upstream Job'])).thenReturn(TEST_UPSTREAM_JOB_VALUE)
		def actualUpStreamId = task.getUpstreamJobId(TEST_JOB_ID)
		assertNotNull(actualUpStreamId)
		assertEquals("The expected upstream Job Id did not match the actual.", TEST_UPSTREAM_JOB_ID, actualUpStreamId)
	}

	@Test
	void shouldGetUpstreamJobIdHappyPath() {

		final TEST_UPSTREAM_JOB_ID = "4567890"
		final TEST_JOB_ID = "123456"
		when(mockShellCommand.execute(['ectool', 'getProperty', '/jobs[123456]/report-urls/Upstream Job'])).thenReturn("4567890")
		def actualUpStreamId = task.getUpstreamJobId(TEST_JOB_ID)
		assertNotNull(actualUpStreamId)
		assertEquals("The expected upstream Job Id did not match the actual.", TEST_UPSTREAM_JOB_ID, actualUpStreamId)
	}


	@Test
	void shouldGetNullWhenThereIsNoUpstreamJobProperty() {

		final TEST_JOB_ID = "123456"

		when(mockShellCommand.execute(['ectool', 'getProperty', '/jobs[123456]/report-urls/Upstream Job'])).thenThrow(new ShellCommandException("[NoSuchProperty] test error message"))

		def actualUpStreamId = task.getUpstreamJobId(TEST_JOB_ID)

		assertNull(actualUpStreamId)
	}

	@Test
	void shouldAddUpstreamChangeLogToMarkup() {

		def dummyData = """<tr>
            <td>
                <a href='/commander/link/jobDetails/jobs/1345555'>Project_Name:liveProcedure</a>
            </td>
        </tr>"""

		assertTrue(task.linkMarkupBuilder.nodeIsEmpty);

		def expectedHTML = new XmlSlurper().parseText(dummyData)

		when(mockShellCommand.execute(['ectool', 'getProperties', '--path', "/jobs[1345555]/ecscm_changeLogs", '--recurse', '1'])).thenReturn(getECPropertySheetResp())
		when(mockShellCommand.execute(['ectool', 'getProperty', "/jobs[1345555]/projectName"])).thenReturn("Project_Name")
		when(mockShellCommand.execute(['ectool', 'getProperty', "/jobs[1345555]/liveProcedure"])).thenReturn("liveProcedure")
		when(mockShellCommand.execute(['ectool', 'getProperty', "/jobs[1345555]/report-urls/Upstream Job"])).thenReturn("invalid")

		task.addUpstreamChangeLogToMarkup(JOB_ID)

		assertFalse(task.linkMarkupBuilder.nodeIsEmpty);

		def actualHTML = new XmlSlurper().parseText(task.linkMarkupBuilder.out.out.out.toString())

		assertEquals(expectedHTML, actualHTML)
	}

	@Test
	void shouldNotAddUpstreamChangeLogToMarkup() {

		assertTrue(task.linkMarkupBuilder.nodeIsEmpty);

		when(mockShellCommand.execute(['ectool', 'getProperties', '--path', "/jobs[1345555]/ecscm_changeLogs", '--recurse', '1'])).thenThrow(new ShellCommandException("ShellCommandException generated for test case"))

		try {
			task.addUpstreamChangeLogToMarkup(JOB_ID)
		}
		catch (ShellCommandException e) {
			println e.getMessage()
		}

		assertTrue(task.linkMarkupBuilder.nodeIsEmpty);
	}


	@Test
	void shouldGetECSCMPropertySheetRecords() {

		final TEST_UPSTREAM_JOB_ID = "1345555"

		when(mockShellCommand.execute(['ectool', 'getProperties', '--path', "/jobs[1345555]/ecscm_changeLogs", '--recurse', '1'])).thenReturn(getECPropertyFlat())

		def actualData = task.getECSCMPropertySheetRecords(TEST_UPSTREAM_JOB_ID)

		def expectedData = getPropertiesFlat()

		println actualData

		assertEquals(2, actualData.findAll().size())

		assertEquals(expectedData, actualData.toString())
	}


	@Test
	void shouldBuildChangeLogMarkup() {
		assertTrue(task.changeLogMarkupBuilder.nodeIsEmpty)

		def dummyData = """<response><propertySheet>
            <property>
                <propertyName>Name1</propertyName>
                <value>Value1</value>
            </property>
        </propertySheet></response>"""

		def expectedResponse = """<p>
              <table>
                <tr>
                  <td>Name1</td>
                </tr>
                <tr>
                  <td>
                    <b>Value1</b>
                  </td>
                </tr>
              </table>
            </p>"""

		def expectedHTML = new XmlSlurper().parseText(expectedResponse)

		def upstreamChangeLogs = new XmlSlurper().parseText(dummyData).propertySheet.property

		task.buildChangelogMarkup(upstreamChangeLogs)

		def actualHTML = new XmlSlurper().parseText(task.changeLogMarkupBuilder.out.out.out.toString())

		assertFalse(task.changeLogMarkupBuilder.nodeIsEmpty);

		assertEquals(expectedHTML, actualHTML);
	}

	@Test
	void shouldBuildLinkMarkup() {

		def expectedData = """<tr>
            <td>
                <a href='/commander/link/jobDetails/jobs/$JOB_ID'>return-test</a>
            </td>
        </tr>"""

		def expectedHTML = new XmlSlurper().parseText(expectedData)

		def mockCommandClient = mock(CommanderClient, Mockito.RETURNS_SMART_NULLS)

		when(mockCommandClient.getSegment(JOB_ID)).thenReturn("return-test")

		task.commanderClient = mockCommandClient;

		assertTrue(task.linkMarkupBuilder.nodeIsEmpty)

		task.buildLinkMarkup(JOB_ID)

		assertFalse(task.linkMarkupBuilder.nodeIsEmpty)

		def actualHTML = new XmlSlurper().parseText(task.linkMarkupBuilder.out.out.out.toString())

		assertEquals(expectedHTML, actualHTML)
	}

	@Test
	void shouldWriteChangeLogMarkup() {

		def dummyData = """<html>
          <head>
              <title>EC:Upstream ChangeLog Report:</title>
          </head>
          <body>
            <h1>Upstream ChangeLog Report</h1>
            <h2>
              <table>
                <tr>
                  <td>
                    <a href='/commander/link/jobDetails/jobs/1345555'>this Job: Test Segment</a>
                  </td>
                </tr>
              </table>
            </h2>
          </body>
        </html>"""

		when(mockShellCommand.execute(['ectool', 'getProperty', "/jobs[1345555]/projectName"])).thenReturn("Project_Name")
		when(mockShellCommand.execute(['ectool', 'getProperty', "/jobs[1345555]/liveProcedure"])).thenReturn("liveProcedure")

		def mockCommandClient = mock(CommanderClient, Mockito.RETURNS_SMART_NULLS)

		when(mockCommandClient.getCurrentSegment()).thenReturn("Test Segment")

		task.commanderClient = mockCommandClient

		def tempFile = File.createTempFile("temp", "tmp")

		println "File generated for test is $tempFile"

		Writer writer = new FileWriter(tempFile);

		assertEquals(0, tempFile.size())

		def expectedHTML = new XmlSlurper().parseText(dummyData);

		task.writeChangeLogMarkup(writer)

		assertTrue(tempFile.size() > 0)

		def actualHTML = new XmlSlurper().parse(tempFile);

		assertEquals(expectedHTML, actualHTML)

	}

	private static String getECPropertySheetResp() {
		return """<response requestId="1" nodeId="10.105.68.77">
    <propertySheet>
      <propertySheetId>2e4857a4-47ab-11e4-963a-00505625f614</propertySheetId>
      <createTime>2014-09-29T07:35:26.928Z</createTime>
      <lastModifiedBy>project: ECSCM-2.2.3.76235</lastModifiedBy>
      <modifyTime>2014-09-29T07:35:26.928Z</modifyTime>
      <owner>project: ECSCM-2.2.3.76235</owner>
      <property>
        <propertyId>2e4857a6-47ab-11e4-963a-00505625f614</propertyId>
        <propertyName>Git-gap-gradle-plugin.git</propertyName>
        <createTime>2014-09-29T07:35:26.928Z</createTime>
        <expandable>1</expandable>
        <lastModifiedBy>project: ECSCM-2.2.3.76235</lastModifiedBy>
        <modifyTime>2014-09-29T07:35:26.928Z</modifyTime>
        <owner>project: ECSCM-2.2.3.76235</owner>
        <value>Commit: 07412e8b01a3a384987702650ea9afe716d73ed4
Author: krishnarangavajhala
Summary: ITCI-1170 [Krishna] testing the upstream job link with testUpstreamJob property.
Date: Mon Sep 29 00:33:58 2014 -0700
</value>      </property>

 <property>
        <propertyId>2e4857a6-47ab-11e4-963a-00505625f614</propertyId>
        <propertyName>test svn</propertyName>
        <createTime>2014-09-29T07:35:26.928Z</createTime>
        <expandable>1</expandable>
        <lastModifiedBy>project: ECSCM-2.2.3.76235</lastModifiedBy>
        <modifyTime>2014-09-29T07:35:26.928Z</modifyTime>
        <owner>project: ECSCM-2.2.3.76235</owner>
        <value>1Commit: 07412e8b01a3a384987702650ea9afe716d73ed4
Author: krishnarangavajhala
Summary: ITCI-1170 [Krishna] testing the upstream job link with testUpstreamJob property.
Date: Mon Sep 29 00:33:58 2014 -0700
</value>      </property>


 <property>
        <propertyId>2e4857a6-47ab-11e4-963a-00505625f614</propertyId>
        <propertyName>test vcs</propertyName>
        <createTime>2014-09-29T07:35:26.928Z</createTime>
        <expandable>1</expandable>
        <lastModifiedBy>project: ECSCM-2.2.3.76235</lastModifiedBy>
        <modifyTime>2014-09-29T07:35:26.928Z</modifyTime>
        <owner>project: ECSCM-2.2.3.76235</owner>
        <value>2Commit: 07412e8b01a3a384987702650ea9afe716d73ed4
Author: krishnarangavajhala
Summary: ITCI-1170 [Krishna] testing the upstream job link with testUpstreamJob property.
Date: Mon Sep 29 00:33:58 2014 -0700
</value>      </property>

 </propertySheet>  </response>"""

	}

	private static String getECPropertyFlat() {
		return """<response requestId="1" nodeId="10.105.68.77">
    <propertySheet>
      <property>
        <propertyId>A</propertyId>
      </property>
      <property>
        <propertyId>B</propertyId>
      </property>
 </propertySheet></response>"""

	}

	private static String getPropertiesFlat() {
		return "AB"
	}


	@Test
	void shouldGenerateHtml() {
		def props = [[propertyName: "prop1", value: "value1"], [propertyName: "prop2", value: "value2"]]
		props.each { p ->
			println p.propertyName.toString()
			println p.value.toString()
		}

		StringWriter out = new StringWriter()
		def builder = new MarkupBuilder(out)
		builder.html {
			head {
				title "test html"
			}
			body {
				h1 "Upstream ChangeLog Report"
				h2 {
					table {
						tr {
							td {
								a(href: '/commander/link/jobDetails/jobs/b406d50c-48e3-11e4-aa20-00505625f614', "Upstream Ecscm ChangeLog Trigger Segment Job Link")
							}
						}
						tr {
							td {
								a(href: '/commander/link/jobDetails/jobs/b406d50c-48e3-11e4-aa20-00505625f614', "This Report Segment Job Link")
							}
						}
					}
				}
				props.each { prop ->
					p {
						table {
							tr {
								td {
									mkp.yield prop.propertyName.toString()
								}
								td {
									b prop.value.toString()
								}
							}
						}
					}
				}
				p "some more text"
			}
		}
		println out.toString()
	}

}
