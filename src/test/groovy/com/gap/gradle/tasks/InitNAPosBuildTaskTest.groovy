package com.gap.gradle.tasks

import static org.junit.Assert.*

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class InitNAPosBuildTaskTest {
    Project project
    private Task task

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

        //creating manifest
        def manifestDir = new File(project.appCodeBase + "/META-INF")
        manifestDir.mkdir()

        def manifest = new File(project.appCodeBase+"/META-INF/MANIFEST.MF")
        manifest.createNewFile()
        FileUtils.writeStringToFile(manifest, "Manifest-version: 1.0\n" +
                "\n" +
                "Name: com/extendyourstore/PACKAGE/\n" +
                "Specification-Title:  Root Package\n" +
                "Specification-Version: Gap-POS\n" +
                "Specification-Vendor: 360Commerce\n" +
                "Implementation-Title: com.extendyourstore.PACKAGE\n" +
                "Implementation-Version: 11.4.9\n" +
                "Implementation-Vendor: 360Commerce\n" +
                "JDK-Compiler: holi")


		project.apply plugin: 'java'
        project.apply plugin: 'gapathena'
        task = project.tasks.findByName('initNAPosBuild')


        project.javado = new File(project.appCodeBase+"/javado")
        project.javado.mkdir()

        task.execute()
    }

    @Test
    void directoryCreationTest() {

        assertTrue("The <appCodeBase>/classes Dir was not created", project.classesDir.isDirectory())
        assertTrue("The <appCodeBase>/test/classes Dir was not created", project.testDir.isDirectory())
        assertTrue("The <appCodeBase>/deprecation/classes Dir was not created", project.deprecation.isDirectory())
		assertTrue("The <appCodeBase>/dist was not created", project.distDir.isDirectory())

        assertFalse("The <appCodeBase>/javado Dir was not deleted",(new File(project.out_javadoc_dir)).isDirectory())

    }

    @Test
    void sourceSetSetTest(){

        assertEquals("The SourceSet was not created correctly ",project.sourceSets.main.output.classesDir.toString(),(tempFolder.root.path + "/classes").toString())

    }
	
	
	
}
