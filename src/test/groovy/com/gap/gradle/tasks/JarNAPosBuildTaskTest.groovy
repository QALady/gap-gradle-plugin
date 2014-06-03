package com.gap.gradle.tasks

import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static org.junit.Assert.*

class JarNAPosBuildTaskTest {
    Project project
    private Task task

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Before
    void setUp() {
        this.project = new ProjectBuilder().builder().build()

        project.apply plugin: 'java'
        project.apply plugin: 'gapathena'

        task = project.tasks.findByName('jarNAPosBuild')

        //properties load

        project.appCodeBase = tempFolder.root.path

        //manifest values
        project.ext['project.shortname'] = "shortNameTest"
        project.ext['project.buildNumber'] = "bn000"
        project.ext['jdk_compiler'] = "testCompiler"

        //folder values
        project.ext['out.jars.dir'] = project.appCodeBase + "/dist"
        project.ext['src.source.dir'] = project.appCodeBase + "/src"
        project.ext['src.classes.dir'] = project.appCodeBase + "classes"
        project.ext['src.debug.dir'] = project.appCodeBase + "debug"
        project.ext['src.3rdparty.jars'] = project.appCodeBase + "/3rdparty/lib"

        //folder structure
        def classesDir = new File(project.properties['src.classes.dir'])
        classesDir.mkdirs()
        project.meta_inf = new File(project.appCodeBase + "/META-INF")
        project.meta_inf.mkdir()
        def ThirdPartyJarDir = new File(project.properties['src.3rdparty.jars'])
        ThirdPartyJarDir.mkdirs()

        //create the fake jars
        def fakeThirdLib = new File(project.properties['src.3rdparty.jars']+"/3rdTestLib.jar")
        fakeThirdLib.createNewFile()

        //manifest file write
        def manifets_org = tempFolder.newFile("/META-INF/MANIFEST.MF")
        FileUtils.writeStringToFile(manifets_org, "Manifest-version: 1.0\n" +
                "\n" +
                "Name: com/extendyourstore/PACKAGE/\n" +
                "Specification-Title:  Root Package\n" +
                "Specification-Version: Gap-POS\n" +
                "Specification-Vendor: 360Commerce\n" +
                "Implementation-Title: com.extendyourstore.PACKAGE\n" +
                "Implementation-Version: 11.4.9\n" +
                "Implementation-Vendor: 360Commerce\n" +
                "JDK-Compiler: holi")
    }


    @Test
    void manifestTest() {

        task.execute()

        def manifest = new File(project.appCodeBase + "/META-INF/MANIFEST.MF")

        if (manifest.isFile()) {
            manifest.eachLine {
                println it
            }
        }
        assertTrue("manifest didn't created ",manifest.isFile())
    }


    @Test
    void jar_sourceTest() {

        //jar names properties
        project.ext['project.jar.src.name'] = "gappmsource.jar"

        task.execute()

        def jarCreated = new File(project.properties['out.jars.dir']+"/" +project.properties['project.jar.src.name'])

        /*
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
        printFiles(project.properties['out.jars.dir'])
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
        */

        assertTrue("jar doesn't exist !!",jarCreated.isFile())

    }

    @Test
    void jar_resTest() {

        //jar names properties
        project.ext['project.jar.res.name'] = "gaprestest.jar"
        //project.ext['project.jar.src.name'] = "gappmsource.jar"

        task.execute()

        def jarCreated = new File(project.properties['out.jars.dir']+"/" +project.properties['project.jar.res.name'])
        //def jarCreated2 = new File(project.properties['out.jars.dir']+"/" +project.properties['project.jar.src.name'])

        println project.properties['out.jars.dir']+"/" +project.properties['project.jar.res.name']
        /*
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
        printFiles(project.properties['out.jars.dir'])
        println ">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"
        */

        //assertTrue("jar doesn't exist !!",jarCreated2.isFile())
        assertTrue("jar doesn't exist !!",jarCreated.isFile())

    }


    private printFiles(String file){
        for(i in (new File(file)).listFiles()){
            println "   >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>"+i
        }
    }

}