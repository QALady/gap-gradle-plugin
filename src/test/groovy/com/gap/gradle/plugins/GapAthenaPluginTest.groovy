package com.gap.gradle.plugins

import ch.qos.logback.core.util.FileUtil
import org.gradle.api.java.archives.Attributes

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
			"project.shortname=testshortname\nproject.buildNumber=test01\nproject.version=test001")

        def depends = tempFolder.newFile("depends.properties")
        FileUtils.writeStringToFile(depends, "classpath.addJar=")

        //creating project structure
        project.javado = new File(project.appCodeBase+"/javado")
        project.dist = new File(project.appCodeBase+"/dist")
        project.javado.mkdir()
        project.dist.mkdir()

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
    }
	
   @Test
    void shouldExistTask_uploadBuildRpmToRepo() {
		taskShouldExist('uploadBuildRpmToRepo')
    }

	@Test
	void shouldExistTask_initNAPosBuild() {
		taskShouldExist('initNAPosBuild')
	}

    @Test
    void loadPropertiesTest(){

        assertEquals("debug level property not set as expected", ("test").toString(), project.project_fullname.toString())

        assertEquals("debug level property not set as expected", ("none").toString(), project.debuglevel.toString())

        assertEquals("Project Dir not set as expected", (tempFolder.root.path).toString(), project.src_dir.toString())
        assertEquals("Classes Dir not set as expected", (tempFolder.root.path + "/classes").toString(), project.src_classes_dir.toString())
        assertEquals("Java Source Dir not set as expected", (tempFolder.root.path+"/src").toString(), project.src_source_dir.toString())
        assertEquals("3rdparty Dir not set as expected", (tempFolder.root.path+"/3rdparty/lib").toString(), project.src_3rdparty_jars.toString())
        assertEquals("Out Dir not set as expected", (tempFolder.root.path).toString(), project.out_dir.toString())
        assertEquals("Test Dir not set as expected", (tempFolder.root.path+"/test").toString(), project.out_test_dir.toString())
        assertEquals("JavaDoc Dir not set as expected", (tempFolder.root.path+"/javado").toString(), project.out_javadoc_dir.toString())

        assertEquals("Out jars Dir not set as expected", (tempFolder.root.path+"/dist").toString(), project.out_jars_dir.toString())
        assertEquals("Out depprojs Dir not set as expected", (tempFolder.root.path+"/dependentProjects").toString(), project.out_depprojs_dir.toString())
        assertEquals("Out Report Dir not set as expected", (tempFolder.root.path+"/reports").toString(), project.out_report_dir.toString())

        assertEquals("Examples Dir not set as expected", (tempFolder.root.path+"/examples").toString(), project.examples_dir.toString())
        assertEquals("Test Dir not set as expected", (tempFolder.root.path+"/test").toString(), project.test_dir.toString())
        assertEquals("Deprecation Dir not set as expected", (tempFolder.root.path+"/deprecation").toString(), project.deprecation_dir.toString())
        assertEquals("Common Properties File not set as expected", (tempFolder.root.path+"/common.properties").toString(), project.common_file.toString())

        assertEquals("Debug Properties File not set as expected", (tempFolder.root.path+"/debug").toString(), project.src_debug_dir.toString())
    }

    @Test
    void createManifestTest(){

        String sectionName = 'com/extendyourstore/PACKAGE/'

        def attrs = project.sharedManifest.getSections().get(sectionName)

        assertNotNull("Manifest-version was not set", attrs.containsKey("Manifest-version"))
        assertNotNull("Specification-Title was not set", attrs.get("Specification-Title"))
        assertNotNull("Specification-Version was not set", attrs.get("Specification-Version"))
        assertNotNull("Implementation-Title was not set", attrs.get("Implementation-Title"))
        assertNotNull("Implementation-Version was not set", attrs.get("Implementation-Version"))
        assertNotNull("Implementation-Vendor was not set", attrs.get("Implementation-Vendor"))
        assertNotNull("JDK-Compiler was not set", attrs.get("JDK-Compiler"))

    }

	def taskShouldExist(task) {
		assertThat(project.tasks.findByName(task), notNullValue())
	}

}
