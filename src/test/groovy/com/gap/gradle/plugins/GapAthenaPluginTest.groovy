package com.gap.gradle.plugins

import static org.hamcrest.Matchers.*
import static org.junit.Assert.*

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class GapAthenaPluginTest {

	private Project project

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
    void setUp() {
        this.project = new ProjectBuilder().builder().build()
       
        project.appCodeBase = tempFolder.root.path

        def randomPropFile = tempFolder.newFile("randomPropFile.properties")
        FileUtils.writeStringToFile(randomPropFile, "project.fullname=test\n" +
			"project.shortname=testshortname\nproject.buildNumber=test01\nproject.version=test001\nproject.jar.main.name=.\n" + 
			"project.jar.src.name=testsource\nproject.jar.res.name=testresource\nproject.jar.config.name=testconfig\n" + 
			"project.jar.test.name=test\nproject.jar.test.src.name=test\nproject.jar.test.res.name=test")

        def depends = tempFolder.newFile("depends.properties")
        FileUtils.writeStringToFile(depends, "classpath.addJar=")

		project.apply plugin: 'java'
        project.apply plugin: 'gapathena'
		
        project.javado = new File(project.appCodeBase+"/javado")
        project.dist = new File(project.appCodeBase+"/dist")

        project.javado.mkdir()
        project.dist.mkdir()
    }
	
   @Test
    void shouldExistTask_uploadBuildRpmToRepo() {
		taskShouldExist('uploadBuildRpmToRepo')
    }

	@Test
	void shouldExistTask_initNAPosBuild() {
		taskShouldExist('initNAPosBuild')
	}

	def taskShouldExist(task) {
		assertThat(project.tasks.findByName(task), notNullValue())
	}

}
