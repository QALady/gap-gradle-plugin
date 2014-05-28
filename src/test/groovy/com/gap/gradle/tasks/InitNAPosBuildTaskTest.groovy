package com.gap.gradle.tasks

import static org.junit.Assert.*

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
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
        project.apply plugin: 'gapathena'
        project.apply plugin: 'java'
        task = project.tasks.findByName('initNAPosBuild')

        project.appCodeBase = tempFolder.root.path

        def randomPropFile = tempFolder.newFile("randomPropFile.properties")
        FileUtils.writeStringToFile(randomPropFile, "project.fullname=test")

        def depends = tempFolder.newFile("depends.properties")
        FileUtils.writeStringToFile(depends, "classpath.addJar=")

        project.javado = new File(project.appCodeBase+"/javado")
        project.dist = new File(project.appCodeBase+"/dist")

        project.javado.mkdir()
        project.dist.mkdir()

        task.execute()
    }



    @Test
    void loadPropertiesTest(){

        assertEquals("debug level property not set as expected", ("test").toString(), project.ext['project.fullname'].toString())

        assertEquals("debug level property not set as expected", ("none").toString(), project.ext['debuglevel'].toString())

        assertEquals("Project Dir not set as expected", (tempFolder.root.path).toString(), project.ext['src.dir'].toString())
        assertEquals("Classes Dir not set as expected", (tempFolder.root.path + "/classes").toString(), project.ext['src.classes.dir'].toString())
        assertEquals("Java Source Dir not set as expected", (tempFolder.root.path+"/src").toString(), project.ext['src.source.dir'].toString())
        assertEquals("3rdparty Dir not set as expected", (tempFolder.root.path+"/3rdparty/lib").toString(), project.ext['src.3rdparty.jars'].toString())
        assertEquals("Out Dir not set as expected", (tempFolder.root.path).toString(), project.ext['out.dir'].toString())
        assertEquals("Test Dir not set as expected", (tempFolder.root.path+"/test").toString(), project.ext['out.test.dir'].toString())
        assertEquals("JavaDoc Dir not set as expected", (tempFolder.root.path+"/javado").toString(), project.ext['out.javadoc.dir'].toString())

        assertEquals("Out jars Dir not set as expected", (tempFolder.root.path+"/dist").toString(), project.ext['out.jars.dir'].toString())
        assertEquals("Out depprojs Dir not set as expected", (tempFolder.root.path+"/dependentProjects").toString(), project.ext['out.depprojs.dir'].toString())
        assertEquals("Out Report Dir not set as expected", (tempFolder.root.path+"/reports").toString(), project.ext['out.report.dir'].toString())

        assertEquals("Examples Dir not set as expected", (tempFolder.root.path+"/examples").toString(), project.ext['examples.dir'].toString())
        assertEquals("Test Dir not set as expected", (tempFolder.root.path+"/test").toString(), project.ext['test.dir'].toString())
        assertEquals("Deprecation Dir not set as expected", (tempFolder.root.path+"/deprecation").toString(), project.ext['deprecation.dir'].toString())
        assertEquals("Common Properties File not set as expected", (tempFolder.root.path+"/common.properties").toString(), project.ext['common.file'].toString())

        assertEquals("Debug Properties File not set as expected", (tempFolder.root.path+"/debug").toString(), project.ext['src.debug.dir'].toString())

    }

    @Test
    void directoryCreationTest(){

        project.classesDir = new File(project.ext['src.classes.dir'])
        project.testDir = new File(project.ext['test.dir']+"/classes")
        project.deprecation = new File(project.ext['deprecation.dir']+"/classes")

        assertTrue("The <appCodeBase>/classes Dir was not created", project.classesDir.isDirectory())
        assertTrue("The <appCodeBase>/test/classes Dir was not created", project.testDir.isDirectory())
        assertTrue("The <appCodeBase>/deprecation/classes Dir was not created", project.deprecation.isDirectory())

        assertFalse("The <appCodeBase>/javado Dir was not deleted",(new File(project.appCodeBase+"/javado")).isDirectory())
        assertFalse("The <appCodeBase>/dist Dir was not deleted",(new File(project.appCodeBase+"/dist")).isDirectory())

    }

    @Test
    void sourceSetSetTest(){

        assertEquals("The SourceSet was not created correctly ",project.sourceSets.main.output.classesDir.toString(),(tempFolder.root.path + "/classes").toString())

    }
	
	
	
}
