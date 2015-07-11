package com.gap.gradle.tasks

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.SegmentRegistry
import com.gap.pipeline.utils.EnvironmentStub
import groovy.json.JsonSlurper
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

	private Project project

	private EnvironmentStub environmentStub

	private CommanderClient commanderClient

	private ShellCommand mockShellCommand

	private SegmentRegistry segmentRegistry

	private CreateHtmlWithGoodVersionsTask task

	private static
	final String goodVersionsMockJsonFile = "src/test/groovy/com/gap/gradle/resources/testSegmentGoodVersionsMock.json"
	private static final String TEST_SEGMENT_IDENTIFIER = "Dummy Project:Dummy Procedure"

	private static
	final String createHtmlGoodVersionFile = "src/test/groovy/com/gap/gradle/resources/testCreateHtmlGoodVersionsTask.html"


	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder()

	@Before
	void setup() {

		project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build();

		project.apply plugin: 'gap-wm-manual'

		def ivyDependenciesOut = """net.sourceforge.cobertura:cobertura
org.codehaus.groovy:groovy-all"""

		environmentStub = new EnvironmentStub();

		mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)

		when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/ivyDependencies'])).thenReturn(ivyDependenciesOut)

		when(mockShellCommand.execute(['ectool', 'getProperty', "/server/watchmen_config/sharedHtdocs"])).thenReturn("/tmp/test/")

		commanderClient = new CommanderClient(mockShellCommand, environmentStub)

		segmentRegistry = new SegmentRegistry(commanderClient)

		project.metaClass.segmentIdentifier = "Test Project:Test Procedure"

		task = new CreateHtmlWithGoodVersionsTask(project, commanderClient, segmentRegistry)

	}

	@Test
	void shouldGetIvyDependencies() {
		String[] expectedIvyDependencies = ["net.sourceforge.cobertura:cobertura", "org.codehaus.groovy:groovy-all"]
		def actualIvyDependencies = task.getIvyDependencies()

		assertEquals(expectedIvyDependencies, actualIvyDependencies)
	}

	@Test
	void shouldBuildDependenciesHtml() {

		mockForBuildDependenciesHtml()

		def actualDependenciesHtml = task.buildDependenciesHtml()

		def tagsActualDependenciesHtml = new XmlSlurper().parseText("<html>$actualDependenciesHtml</html>")

		def contentOfCreateHtmlGoodVersionFile = new File(createHtmlGoodVersionFile).getText()

		def tagsExpectDependenciesHtml = new XmlSlurper().parseText("<html>$contentOfCreateHtmlGoodVersionFile</html>")

		assertEquals(tagsExpectDependenciesHtml, tagsActualDependenciesHtml)
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
		when(mockShellCommand.execute(['ectool', 'getProperty', "/projects[WM Segment Registry]/IdentifierRegistry/net.sourceforge.cobertura:cobertura/segment"])).thenReturn(TEST_SEGMENT_IDENTIFIER)
		when(mockShellCommand.execute(['ectool', 'getProperty', "/projects[WM Segment Registry]/IdentifierRegistry/org.codehaus.groovy:groovy-all/segment"])).thenReturn(TEST_SEGMENT_IDENTIFIER)

		when(mockShellCommand.execute(['ectool', '--format', 'json', 'getProperties', '--path', '/projects[WM Segment Registry]/SegmentRegistry/Dummy Project:Dummy Procedure/goodVersions', '--recurse', '1'])).thenReturn(getMockData())
	}

	private def testMethod(Map pConfig) {
		logger.info("TestMethod")
		logger.info("${pConfig.path}")
		logger.info("${pConfig.recurse}")
		logger.info(pConfig.toString())
		logger.info(pConfig.toMapString())
	}

	@Test
	void testTestMethod() {
		testMethod([path: "abcd", recurse: 1])
	}

	@Test
	void testReadingSlurpedJson() {
		def slurpedJson = getSlurpedMockJson()
		def versions = slurpedJson.propertySheet.property.propertyName.toArray()
		def resolvedDependencies = [:]
		logger.info("Versions Array: $versions")
		versions.each { version ->
			def data = slurpedJson.propertySheet.property.find { it.propertyName == version }
			def propData = data.propertySheet.property.find { it.propertyName == "resolvedDependencies" }
			resolvedDependencies.put(version, propData.value.toString())
			logger.info("Resolved Deps: <pre> ${resolvedDependencies[version]} </pre>")
		}
	}

	@Test
	void shouldPopulateVersionsExceptLatest() {
		def expectedData = """<root><option value="dummy:1">1</option>
        <option value="dummy:2">2</option>
        <option value="dummy:3">3</option>
        <option value="dummy:5">5</option>
        <option value="dummy:4">4</option></root>"""

		def versions = ['1', '2', '3', '6', '5', '4']
		def latestVersion = '6'
		def dependency = "dummy"
		def resolvedDependencies = ['1': 'One', '2': 'Two', '3': 'Three', '4': 'Four', '5': 'Five', '6': 'Six']
		def actualData = task.populateVersionsExceptLatest(versions, latestVersion, dependency, resolvedDependencies)
		def actualDataWrapped = "<root>$actualData</root>"
		def expectedDataHtml = new XmlSlurper().parseText(expectedData)
		def actualDataHtml = new XmlSlurper().parseText(actualDataWrapped)

		logger.info("Row is :$actualData")

		assertEquals(versions.size() - 1, actualData.split("\n").size())
		assertEquals(expectedDataHtml, actualDataHtml)
	}


	private static def getSlurpedMockJson() {
		return new JsonSlurper().parseText(new File(goodVersionsMockJsonFile).getText())
	}

	private static String getMockData() {
		return new File(goodVersionsMockJsonFile).getText()
	}

}
