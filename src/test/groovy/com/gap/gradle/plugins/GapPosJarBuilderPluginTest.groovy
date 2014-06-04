package com.gap.gradle.plugins

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.hamcrest.Matchers.notNullValue
import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertThat
import static org.junit.Assert.assertTrue

class GapPosJarBuilderPluginTest {

	private Project project

	@Rule
	public TemporaryFolder tempFolder = new TemporaryFolder();
	
	@Before
    void setUp() {
        this.project = new ProjectBuilder().builder().build()
       
        project.appCodeBase = tempFolder.root.path

        def randomPropFile = tempFolder.newFile("randomPropFile.properties")
        FileUtils.writeStringToFile(randomPropFile, "project.fullname=test\nproject.brand=project_brand\n" +
			"project.shortname=testshortname\nproject.buildNumber=test01\nproject.version=test001\nproject.jar.main.name=.\n" + 
			"project.jar.src.name=testsource\nproject.jar.res.name=testresource\nproject.jar.config.name=testconfig\n" + 
			"project.jar.test.name=test\nproject.jar.test.src.name=test\nproject.jar.test.res.name=test")


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


        def depends = tempFolder.newFile("depends.properties")
        FileUtils.writeStringToFile(depends, "classpath.addJar=")

		project.apply plugin: 'java'
        project.apply plugin: 'gapathena'
        project.apply plugin: 'gapposjar'


        project.javado = new File(project.appCodeBase+"/javado")
        project.dist = new File(project.appCodeBase+"/dist")

        project.javado.mkdir()
        project.dist.mkdir()



    }
	
   @Test
   void gapPosJarBuildTest() {

       testJar("mainJar")
       testJar("sourceJar")
       testJar("resourceJar")
       testJar("configJar")
       testJar("testMainJar")
       testJar("testSrcJar")
       testJar("testResJar")

   }


    void testJar(String taskName) {

       def jarTask = project.tasks.findByName(taskName)
        jarTask.execute()
/*
       for(files in project.fileTree(dir: project.appCodeBase)){
           println files
       }

       println ""
       println mainJarTask.archiveName
       println ""
       println project.appCodeBase+"/dist/"+mainJarTask.archiveName
       println ""
*/
       File createdJar = new File(project.appCodeBase+"/dist/"+jarTask.archiveName)

       assertTrue("Jar was not generated for task : " + taskName, createdJar.isFile())
    }

	private void taskShouldExist(task) {
		assertThat(project.tasks.findByName(task), notNullValue())
	}

}
