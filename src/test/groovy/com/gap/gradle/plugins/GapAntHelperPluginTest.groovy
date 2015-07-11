package com.gap.gradle.plugins

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import org.apache.commons.io.FileUtils
import org.apache.tools.ant.types.Path
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GapAntHelperPluginTest {

	private Project project
	private static final String sampleJarFile = "com/gap/gradle/resources/sample-ant-classpath-import-jar"
	
   @Rule
   public TemporaryFolder tempFolder = new TemporaryFolder();

   @Test
    void testAntPathInjection() {
        project = ProjectBuilder.builder().build()
        project.metaClass.antDepConf = "athenadeps"
        project.metaClass.antPathName = "build.classpath"
        project.configurations {
            athenadeps
        }
        project.dependencies {
            athenadeps project.files(sampleJarFile)
        }
		
		def buildFile = tempFolder.newFile("test-build.xml")
		FileUtils.writeStringToFile(buildFile, '<project default="development" basedir="."><path id="build.classpath"></path><target name="development"></target></project>')
 
		project.ant.importBuild  buildFile

		project.apply plugin: 'gapant'
		
		Path actualPath = project.ant.getReferences().get("build.classpath")
		assertNotNull(actualPath)
		assertTrue(actualPath.list()[0].contains(sampleJarFile))

    }
}
