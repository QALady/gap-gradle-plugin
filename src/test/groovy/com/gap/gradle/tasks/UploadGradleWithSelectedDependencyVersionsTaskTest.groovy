package com.gap.gradle.tasks

import static org.junit.Assert.*
import static org.mockito.Mockito.*

import java.util.regex.Pattern

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.mockito.Mockito

import com.gap.pipeline.ec.CommanderClient
import com.gap.pipeline.ec.SegmentRegistry

class UploadGradleWithSelectedDependencyVersionsTaskTest {
	Project project
	UploadGradleWithSelectedDependencyVersionsTask task
	private String selectedVersions = "com.gap.test:test-app:1.0.123,com.gap.testing:test-app:2.2.456"
	private def segmentIdentifier = "Test Project:Test Procedure"
	private def versions
	private SegmentRegistry mockSegmentRegistry
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder()
	String testGradleFile
	
	@Before
	void setup() {
		project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build();

		testGradleFile = "$temporaryFolder.root.path/test.gradle"
		println testGradleFile

		setupGradleFile()
		mockSegmentRegistry = mock(SegmentRegistry, Mockito.RETURNS_SMART_NULLS)
		
		project.apply plugin: 'gapmanual'
		//task = project.tasks.findByName('linkUpstreamChangelogReport')
		task = new UploadGradleWithSelectedDependencyVersionsTask(project, new CommanderClient(), mockSegmentRegistry)
		versions = selectedVersions.split(",")
	}	
	
	void setupGradleFile() {
		File test = new File(testGradleFile)
		def writer = test.newWriter()
		writer.write("""
				
				dependencies {
					segment group: 'com.gap.test', name: 'test-app', version: '4.84.44', configuration: 'archives'
					segment group: 'com.gap.testing', name: 'test-app', version: '0000', configuration: 'archives'
				}
							
					""".toString())
		writer.close()
	}
	
	@Ignore
	void shouldReplaceGradleWithSelectedVersions() {
		when(mockSegmentRegistry.getSegmentRegistryValue(segmentIdentifier, "gradleFile")).thenReturn("$temporaryFolder.root.path/test.gradle")
		task.replaceGradleWithSelectedVersions()
		assertNotNull("$temporaryFolder.root.path/test.gradle")
		def artifactMetadata = versions[0].split(":")
		def regexExpected = "group: \'$artifactMetadata[0]\', name: \'$artifactMetadata[1]\', version: \$artifactMetadata[2]\'"
		
	}
	
	@Test
	void shouldMatchPatterns() {
		def pattern = ~/group\s*:\s*'\S+',\s*name\s*:\s*'\S+',\s*version\s*:\s*'\d+(\.?\d)*'/
		assert pattern.matcher("group      :       'com.gap.test',name  : 'test-app',     version:  '2.4545'").matches()
	}
	
	@Test
	void shouldMatchPatternsInFile() {
		File gradleFile = new File(testGradleFile)
 		println "Gradle File before"
		println gradleFile.getText()
		//def pattern = ~/group\s*:\s*'com.gap.test',\s*name\s*:\s*'test-app',\s*version\s*:\s*'\d+(\.?\d)*'/
		//assert pattern.matcher("group: 'com.gap.test', name: 'test-app', version: '0000'").matches()
		
		//patterns =  [pattern1 : constantPart1ConfigurablePart1,
			//pattern2 : constantPart2ConfigurablePart2,
			//pattern3 : constantPart3ConfigurablePart3]
		def patterns = [:]
		versions.each { selectedVersion ->
			// example: each selected version is like com.gap.test:test-app:1.0.123
			def versionSplit = selectedVersion.split(":")
			def pttrn = ~/group\s*:\s*'${versionSplit[0]}',\s*name\s*:\s*'${versionSplit[1]}',\s*version\s*:\s*'\d+(\.?\d)*'/
			def replacementString = "group: \'${versionSplit[0]}\', name: \'${versionSplit[1]}\', version: \'${versionSplit[2]}\'"
			println "test:" + replacementString
			patterns.put(pttrn, replacementString)
		}
		
	   StringBuffer stringWriter = new StringBuffer()
	   def ls = System.getProperty('line.separator')
	   gradleFile.eachLine{ line ->
		   patterns.each { pattern, replacement ->
			   line = line.replaceAll(pattern, replacement)
		   }
		   stringWriter.append(line).append(ls)
	   }
	   gradleFile.write(stringWriter.toString())
	   println "gradle file after"
	   println gradleFile.getText()
		
	}

	
}
