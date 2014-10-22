package com.gap.gradle.tasks

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

import java.text.ParseException
import java.text.SimpleDateFormat

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito

import com.gap.gradle.utils.ShellCommand
import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.SegmentRegistry
import com.gap.pipeline.utils.EnvironmentStub

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

		def ivyDependenciesOut = """net.sourceforge.cobertura:cobertura
org.codehaus.groovy:groovy-all"""

		environmentStub = new EnvironmentStub();

		mockShellCommand = mock(ShellCommand, Mockito.RETURNS_SMART_NULLS)

		when(mockShellCommand.execute(['ectool', 'getProperty', '/myJob/ivyDependencies'])).thenReturn(ivyDependenciesOut)

		when(mockShellCommand.execute(['ectool', 'getProperty', "/server/watchmen_config/sharedHtdocs"])).thenReturn("/tmp/test/")
		//when(mockShellCommand.execute(['ectool', 'getProperty', "/server/watchmen_config/sharedHtdocs"])).thenReturn(temporaryFolder)

		commanderClient = new CommanderClient(mockShellCommand, environmentStub)

		segmentRegistry = new SegmentRegistry(commanderClient)

		project.segmentIdentifier = "Test Project:Test Procedure"

		task = new CreateHtmlWithGoodVersionsTask(project, commanderClient, segmentRegistry)

	}

	@Test
	void shouldGetIvyDependencies() {
		String[] expectedIvyDependencies = ["net.sourceforge.cobertura:cobertura", "org.codehaus.groovy:groovy-all"]
		def actualIvyDependencies = task.getIvyDependencies()

		assertEquals(expectedIvyDependencies, actualIvyDependencies)
	}

	@Ignore
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

	@Ignore
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
		when(mockShellCommand.execute(['ectool', 'getProperty', "/projects[WM Segment Registry]/IdentifierRegistry/net.sourceforge.cobertura:cobertura/segment"])).thenReturn("net.sourceforge.cobertura:cobertura")
		when(mockShellCommand.execute(['ectool', 'getProperty', "/projects[WM Segment Registry]/SegmentRegistry/net.sourceforge.cobertura:cobertura/goodVersions/propertySheetId"])).thenReturn("cobertura")

		def coberturaString = '{"propertySheet":[{"property":{"propertyName":"1.1"}}, {"property":{"propertyName":"2.2"}}]}'

		when(mockShellCommand.execute(['ectool', '--format', 'json', 'getProperties', '--key', "cobertura"])).thenReturn(coberturaString)
		when(mockShellCommand.execute(['ectool', 'getProperty', '/projects[WM Segment Registry]/SegmentRegistry/net.sourceforge.cobertura:cobertura/goodVersions/2.2/resolvedDependencies'])).thenReturn("com.gap.watchmen.diamondDependency:diamondDependencyC")

		when(mockShellCommand.execute(['ectool', 'getProperty', "/projects[WM Segment Registry]/IdentifierRegistry/org.codehaus.groovy:groovy-all/segment"])).thenReturn("org.codehaus.groovy:groovy-all")
		when(mockShellCommand.execute(['ectool', 'getProperty', "/projects[WM Segment Registry]/SegmentRegistry/org.codehaus.groovy:groovy-all/goodVersions/propertySheetId"])).thenReturn("groovy-all")

		def groovyAllString = '{"propertySheet":[{"property":{"propertyName":"1.12"}}, {"property":{"propertyName":"2.14"}}]}'

		when(mockShellCommand.execute(['ectool', '--format', 'json', 'getProperties', '--key', "groovy-all"])).thenReturn(groovyAllString)
		when(mockShellCommand.execute(['ectool', 'getProperty', '/projects[WM Segment Registry]/SegmentRegistry/org.codehaus.groovy:groovy-all/goodVersions/2.14/resolvedDependencies'])).thenReturn("com.gap.watchmen.diamondDependency:diamondDependencyB")
	}
	
	private def testMethod(Map pConfig) {
		println "TestMethod"
		println pConfig.path
		println pConfig.recurse
		println pConfig.toString()
		println pConfig.toMapString()
	}
	
	@Test
	void testTestMethod() {
		testMethod([path: "abcd", recurse: 1])
	}
}
