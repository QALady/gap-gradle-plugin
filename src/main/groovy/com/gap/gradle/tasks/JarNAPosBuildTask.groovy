package com.gap.gradle.tasks

import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.ZipEntryCompression

@RequiredParameters([
	@Require(parameter = 'appCodeBase', description = "Absolute Path of NA POS Trunk codebase eg: /workspaces/na-trunk/pos/ProductMigration")
])
class JarNAPosBuildTask extends WatchmenTask {
	def log = LogFactory.getLog(JarNAPosBuildTask)
	def project

    JarNAPosBuildTask(Project project) {
		super(project)
		this.project = project
	}

	def execute() {

        File distDir = new File(project.ext['out.jars.dir'])
        distDir.mkdirs()

        createManifest()
        project.ext['3rdjarfiles'] = ""
        for (file in project.fileTree(dir: project.ext['src.3rdparty.jars'], include: '**/*.jar')) {
            project.ext['3rdjarfiles'] = project.ext['3rdjarfiles'] + " " + file
        }

        if (project.properties.containsKey('project.jar.src.name')) {
            jar_source()
            project.tasks.findByName('jar').execute()
        }

        if (project.properties.containsKey('project.jar.res.name')){
            jar_res()
            project.tasks.findByName('jar').execute()
        }

        if(project.properties.containsKey('project.jar.config.name')){
            jar_config()
            project.tasks.findByName('jar').execute()
        }

        if(project.properties.containsKey('project.jar.main.name')){
            jar_main()
            project.tasks.findByName('jar').execute()
        }

        if(project.properties.containsKey('compile.optimized') || project.properties.hasProperty('integrate.build')){
            jar_integrate_candidate()
            project.tasks.findByName('jar').execute()
        }

        if(project.properties.containsKey('project.jar.one.name')){
            jar_one()
            project.tasks.findByName('jar').execute()
        }

        if(project.properties.containsKey('project.jar.test.name')){
            jar_test()
            project.tasks.findByName('jar').execute()
        }

        if(project.properties.containsKey('project.jar.test.name')){
            jar_test()
            project.tasks.findByName('jar').execute()
        }

        if(project.properties.containsKey('project.jar.test.src.name')){
            jar_test_source()
            project.tasks.findByName('jar').execute()
        }

        if(project.properties.containsKey('project.jar.test.res.name')){
            jar_test_res()
            project.tasks.findByName('jar').execute()
        }

        if(project.properties.containsKey('compile.optimized') || project.properties.containsKey('integrate.build')){
            jar_test_integrate()
            project.tasks.findByName('jar').execute()
        }
	}

    def createManifest() {

        def manifest_org = new File(project.appCodeBase + "/META-INF/MANIFEST.MF.ORG")
        def manifest_mf = new File(project.appCodeBase + "/META-INF/MANIFEST.MF")

        project.jar {

            manifest {

                def attrs = new HashMap<String, String>()

                if (manifest_org.isFile()) {
                    manifest_org.eachLine {
                        if (!it.isEmpty() && it.contains(': ') && !it.contains("Manifest-version:")) {
                            attrs.put(it.split(': ')[0], it.split(': ')[1])
                        }
                    }
                } else if (manifest_mf.isFile()) {
                    manifest_mf.eachLine {
                        if (!it.isEmpty() && it.contains(': ') && !it.contains("Manifest-version:")) {
                            attrs.put(it.split(': ')[0], it.split(': ')[1])
                        }
                    }
                }

                attrs.put('Specification-Version', project.ext['project.shortname'])
                attrs.put('Implementation-Version', project.ext['project.buildNumber'])
                attrs.put('JDK-Compiler', project.ext['jdk_compiler'])

                def sectionName = attrs.get("Name")
                attrs.remove("Name")
                attributes(attrs, sectionName)
            }

            manifest.writeTo(project.appCodeBase+"/META-INF/MANIFEST.MF")
        }
    }

    def jar_source() {
        project.jar {
            destinationDir = new File(project.ext['out.jars.dir'])
            archiveName = project.ext['project.jar.src.name']

            from(project.ext['src.source.dir']) {
                include "**/*.class"
                exclude "**/shopx/**/*.*"
            }
        }
    }

    def jar_res() {

        project.jar {
            destinationDir = new File(project.ext['out.jars.dir'])
            archiveName = project.ext['project.jar.res.name']

            from(project.ext['src.source.dir']) {
                exclude "**/*.java,**/*.class"

            }

            entryCompression  ZipEntryCompression.STORED
        }
    }

    def jar_config(){
        project.jar {
            destinationDir = new File(project.ext['out.jars.dir'])
            archiveName = project.ext['project.jar.config.name']

            from(project.ext['src.dir']){
                include  "config/**,config/*"
            }
        }
    }

    def jar_main(){
        project.jar {
            destinationDir = new File(project.ext['out.jars.dir'])
            archiveName = project.ext['project.jar.main.name']

            from(project.ext['src.classes.dir']){
                include "**/*.class"
                exclude "**/shopx/**/*.*"
            }
            def attrs = new HashMap<String, String>()
            attrs.put('Class-Path',project.ext['3rdjarfiles'])
            manifest.attributes(attrs)
        }
    }

    def jar_integrate_candidate(){

        String destination = project.ext['src.debug.dir']+"/dist"
        createDir(destination)

        project.jar {
            archiveName = project.ext['project.jar.main.name']

            from(project.ext['src.debug.dir']+"/classes"){
                include "**/*.class"
                exclude "**/shopx/**/*.*"
            }
            def attrs = new HashMap<String, String>()
            attrs.put('Class-Path',project.ext['3rdjarfiles'])
            manifest.attributes(attrs)
        }
    }

    def jar_one(){
        project.jar {
            destinationDir = new File(project.ext['out.jars.dir'])
            archiveName = project.ext['project.jar.one.name']

            from(project.ext['src.classes.dir']) {
                include "**/*.class"
                exclude "**/dist/*"
            }

            from(project.ext['src.source.dir']) {
                include "com/**,com/*"
                exclude "com/**/*.java"
            }

            def attrs = new HashMap<String, String>()
            attrs.put('Class-Path', project.ext['3rdjarfiles'])
            manifest.attributes(attrs)
        }
    }

    def jar_test(){
        project.jar {

            destinationDir = new File(project.ext['out.jars.dir'])
            archiveName = project.ext['project.jar.test.name']

            from(project.ext['test.dir']+"/classes") {
                include "**/*.class"
            }
        }
    }

    def jar_test_source(){
        project.jar {

            destinationDir = new File(project.ext['out.jars.dir'])
            archiveName = project.ext['project.jar.test.src.name']

            from(project.ext['test.dir']+"/src") {
                include "**/*.java"
            }
        }
    }

    def jar_test_res(){
        project.jar {

            destinationDir = new File(project.ext['out.jars.dir'])
            archiveName = project.ext['project.jar.test.res.name']

            from(project.ext['test.dir']+"/src") {
                exclude "**/*.java,**/*.class"
            }
        }
    }

    def jar_test_integrate(){

        String destination = project.ext['src.debug.dir']+"/dist"
        createDir(destination)

        project.jar {
            destinationDir = new File(destination)
            archiveName = project.ext['project.jar.test.name']

            from(project.ext['src.debug.dir']+"/test/classes") {
                include "**/*.class"
            }
        }
    }



    private createDir(String dir){
        File newDir = new File(dir)
        if(!newDir.isDirectory()){
            newDir.mkdirs()
        }
    }

}
