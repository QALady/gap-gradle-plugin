package com.gap.gradle.tasks

import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

@RequiredParameters([
	@Require(parameter = 'appCodeBase', description = "Absolute Path of NA POS Trunk codebase eg: /workspaces/na-trunk/pos/ProductMigration")
])
class InitNAPosBuildTask extends WatchmenTask {
	def log = LogFactory.getLog(com.gap.gradle.tasks.InitNAPosBuildTask)
	def project

	InitNAPosBuildTask(Project project) {
		super(project)
		this.project = project
	}

	def execute() {
		validate()
		loadProperties()
		configJavac()
		createDirectories()
		addCustomPathsToSourceSets()
		addCustomCompileDependencies()
		showInfo();
	}

	private addCustomCompileDependencies() {
		project.dependencies {
			compile project.files(getClassPath("${project.appCodeBase}/depends.properties"))
		}
	}

	private addCustomPathsToSourceSets() {
		project.sourceSets {
			main {
				output.classesDir = project.properties['src.classes.dir']

				java {
					srcDir project.properties['src.source.dir']
				}
			}

			test {
				output.classesDir = project.properties['test.dir']+"/classes"

				java {
					srcDirs project.properties['test.dir']+'/src'
				}
			}
		}
	}
	
	def getClassPath(dependsPropertiesFile) {
		
		List jars = new ArrayList<File>()
	
		jars.add(project.properties["src.3rdparty.jars"]+"/ingenctl.jar")
	
		//read the depends from depends.properties
		def config = new Properties()
		def propFile = new File(dependsPropertiesFile)
		if (propFile.canRead()) {
			config.load(new FileInputStream(propFile))
	
			for(file in project.fileTree(dir : project.properties['src.dir']+'/'+config['classpath.addJar'], include: '*.jar')) {
				jars.add(file)
			}
	
			for (Map.Entry property in config) {
				for(file in project.fileTree(dir : project.properties['src.dir']+'/'+property.value+'/dist', include: '*.jar', exclude: ['*source.jar'])) {
					jars.add(file)
				}
			}
		}
	
		//read the depends from ProductMigration/3rdparty/lib
		for(file in project.fileTree(dir : project.properties['src.3rdparty.jars'], include: ['**/*.jar','**/*.zip'])) {
			jars.add(file)
		}
	
		return jars
	}
		

	def createDirectories() {
			project.classesDir = new File(project.properties['src.classes.dir'])
			project.testDir = new File(project.properties['test.dir']+'/classes')
			project.deprecation = new File(project.properties['deprecation.dir']+'/classes')
		
			project.classesDir.mkdir()
			project.testDir.mkdir()
			project.deprecation.mkdir()

			project.delete(project.properties['out.javadoc.dir'], project.properties['out.jars.dir'])			
	}
		
	def configJavac(){
		[project.compileJava, project.compileTestJava]*.options.collect {options -> options.encoding = 'ISO-8859-1'}
		//[compileJava, compileTestJava]*.options.collect {options -> options.debuglevel = project.properties['debuglevel']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.debug = project.properties['debug']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.deprecation = project.properties['deprecation']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.optimize = project.properties['optimize']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.source = project.properties['project.source.flag']}
	}
	
	
	def loadProperties() {
	
			for(file in project.fileTree(dir : "${project.appCodeBase}", include: '*.properties')) {
				loadPropertiesFromFile(file.getAbsolutePath())
			}
			
			project.ext['debuglevel'] = "none"
		
			project.ext['src.dir'] = project.appCodeBase
			project.ext['src.classes.dir'] = "${project.appCodeBase}/classes"
			project.ext['src.source.dir'] = "${project.appCodeBase}/src"
			project.ext['src.3rdparty.jars'] = "${project.appCodeBase}/3rdparty/lib"
			//project.ext['src.3rdparty.programs'] = '3rdpartycode/programs'
		
			project.ext['out.dir'] = project.appCodeBase
			project.ext['out.test.dir'] = "${project.appCodeBase}/test"
			project.ext['out.javadoc.dir'] = "${project.appCodeBase}/javado"
		
			project.ext['out.jars.dir'] = "${project.appCodeBase}/dist"
			project.ext['out.depprojs.dir'] = "${project.appCodeBase}/dependentProjects"
			project.ext['out.report.dir'] = "${project.appCodeBase}/reports"
		
			project.ext['examples.dir'] = "${project.appCodeBase}/examples"
			project.ext['test.dir'] = "${project.appCodeBase}/test"
			project.ext['deprecation.dir'] = "${project.appCodeBase}/deprecation"
			project.ext['common.file'] = "${project.appCodeBase}/common.properties"
		
			project.ext['src.debug.dir'] = "${project.appCodeBase}/debug"
	}
		
	def loadPropertiesFromFile(String sourceFileName) {
		def config = new Properties()
		def propFile = new File(sourceFileName)
		if (propFile.canRead()) {
			config.load(new FileInputStream(propFile))
			for (Map.Entry property in config) {
				project.ext[property.key] = property.value
			}
		}
	}
	
	def showInfo(){
		println "   --------- "+ project.properties['project.fullname'] + " " + project.properties['project.version'] + " ---------------------------"
		println "   --------- "+ project.properties['project.shortname']+"-"+project.properties['project.buildNumber']+ " ---------------------"
		println "   --------- Building Type: " +project.properties['build.type']+ " ---------"
		println ""
		println ""
		println "   java.home       = " + System.properties['java.home']
		println "   user.home       = " + System.properties['user.home']
		println "   src.classes.dir = " + project.properties['src.classes.dir']
		println "   out.jars.dir    = " + project.properties['out.jars.dir']
		println "   appCodeBase     = ${project.appCodeBase} (basedir for ant build)"
		println "   3rdparty.dir    = " + project.properties['src.3rdparty.jars']
		println ""
		println "   ----------------------------------------------"
		println "   Compile configuration dependencies: "
		println ""
		for(deps in project.configurations.compile){
			println "       --"+deps.name
		}
		println "   ----------------------------------------------"
		println ""
		println "   Folders created:"
		println ""
		println "       --"+project.classesDir
		println "       --"+project.testDir
		println "       --"+project.deprecation
		println ""
		println ""
		println "   Folders deleted:"
		println ""
		println "       --"+project.properties['out.javadoc.dir']
		println "       --"+project.properties['out.jars.dir']
		println ""
		println ""
	}
	
}
