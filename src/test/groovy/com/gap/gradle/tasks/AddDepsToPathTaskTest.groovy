package com.gap.gradle.tasks

import static org.junit.Assert.*

import org.apache.commons.io.FileUtils
import org.apache.tools.ant.types.Path
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class AddDepsToPathTaskTest {

	private Project project
	private Task task
	private static final String sampleJarFile = "com/gap/gradle/resources/sample-ant-classpath-import-jar"

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();

	@Before
	void setup() {
		project = ProjectBuilder.builder().build()
		project.apply plugin: 'gapant'
		project.antHelperConfig.dependencyConfigurationName = "athenadeps"
		project.antHelperConfig.antPathName = "build.classpath"
		project.configurations {
			athenadeps
		}
		project.dependencies {
			athenadeps project.files(sampleJarFile)
		}
		task = project.tasks.findByName('addDependencyConfigToAntPath')
	}

	@Test
	void testAddAthenaDepsToAntPathTask() {
		def buildFile = tempFolder.newFile("test-build.xml")
		FileUtils.writeStringToFile(buildFile, '<project default="development" basedir="."><path id="build.classpath"></path><target name="development"></target></project>')

		project.ant.importBuild  buildFile
		task.execute()
		Path actualPath = project.ant.getReferences().get("build.classpath")
		assertNotNull(actualPath)
		assertTrue(actualPath.list()[0].contains(sampleJarFile))
	}
}
