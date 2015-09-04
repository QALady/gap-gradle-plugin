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

@Ignore
class UploadGradleWithSelectedDependencyVersionsTaskTest {
	Project project
	UploadGradleWithSelectedDependencyVersionsTask task
	private def selectedVersions = "com.gap.test:test-app:1.0.123,com.gap.testing:test-app:2.2.456".split(",")
	private def segmentIdentifier = "Test Project:Test Procedure"
	private SegmentRegistry mockSegmentRegistry
	
	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder()
	String testGradleFile
	
	@Before
	void setup() {
		project = ProjectBuilder.builder().withProjectDir(new File(temporaryFolder.root.path)).build();
		testGradleFile = "$temporaryFolder.root.path/test.gradle"
		setupTestGradleFile()
		mockSegmentRegistry = mock(SegmentRegistry, Mockito.RETURNS_SMART_NULLS)
		project.apply plugin: 'gap-wm-manual'
		task = new UploadGradleWithSelectedDependencyVersionsTask(project, new CommanderClient(), mockSegmentRegistry)
	}	
	
	void setupTestGradleFile() {
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
	void whenExecuteShouldUpdateSelectedVersionsToSegmentGradleFile() {
		when(mockSegmentRegistry.getSegmentRegistryValue(segmentIdentifier, "gradleFile")).thenReturn("$temporaryFolder.root.path/test.gradle")
		task.replaceGradleWithSelectedVersions()
		assertNotNull("$temporaryFolder.root.path/test.gradle")
		
	}

	@Test
	void shouldReplaceGradleWithSelectedVersions() {
		task.replaceGradleWithSelectedVersions(testGradleFile, selectedVersions)
		validateGradleFileToContainExpectedVersions(testGradleFile, selectedVersions)
	}	

	private void validateGradleFileToContainExpectedVersions(gradleFile, expectedVersions) {
		def pattern = ~/group\s*:\s*'\S+'\s*,\s*name\s*:\s*'\S+'\s*,\s*version\s*:\s*'\d+(\.?\d)*'/
		def actualVersionsInFile = []
		File handle = new File(gradleFile)
		assertNotNull(handle)
		handle.eachLine{ line ->
			def actualEntry = line.find(pattern)
			if (actualEntry) {
				def actualArtifact = []
				actualEntry.split(",").each { it->
						actualArtifact.add( (it.split(":")[1]).trim().replaceAll (/'/, '') )
				}
				actualVersionsInFile.add(actualArtifact.join(":"))
			}
		}
		assert selectedVersions == actualVersionsInFile
	}

	@Test
	void shouldMatchPatterns() {
		def pattern = ~/group\s*:\s*'\S+'\s*,\s*name\s*:\s*'\S+'\s*,\s*version\s*:\s*'\d+(\.?\d)*'/
		assert pattern.matcher("group      :       'com.gap.test',name  : 'test-app',     version:  '2.4545'").matches()
		assert pattern.matcher("group:'com.gap.test',name:'test-app',version:'2.4545'").matches()
		assert pattern.matcher("group:'com.gap.test' , name:'test-app',version:'2.4545'").matches()
		assert pattern.matcher("group:'com.gap.test' , name:'test-app',version:'24545'").matches()
		assert pattern.matcher("group:'com.gap.test', name : 'test-app', version: '01'").matches()
	}
	
	@Test
	void shouldMatchPatternsInFile() {
		File gradleFile = new File(testGradleFile)
		def patterns = [:]
		selectedVersions.each { selectedVersion ->
			// example: each selected version is like com.gap.test:test-app:1.0.123
			def versionSplit = selectedVersion.split(":")
			def pattern = ~/group\s*:\s*'${versionSplit[0]}'\s*,\s*name\s*:\s*'${versionSplit[1]}'\s*,\s*version\s*:\s*'\d+(\.?\d)*'/
			def replacement = "group: \'${versionSplit[0]}\', name: \'${versionSplit[1]}\', version: \'${versionSplit[2]}\'"
			patterns.put(pattern, replacement)
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
	}
	
}
