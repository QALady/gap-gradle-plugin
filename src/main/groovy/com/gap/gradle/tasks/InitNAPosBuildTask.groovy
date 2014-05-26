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
		loadProperties()
	}

	def configJavac(){
		[compileJava, compileTestJava]*.options.collect {options -> options.encoding = 'ISO-8859-1'}
		//[compileJava, compileTestJava]*.options.collect {options -> options.debuglevel = this.properties['debuglevel']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.debug = this.properties['debug']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.deprecation = this.properties['deprecation']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.optimize = this.properties['optimize']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.source = this.properties['project.source.flag']}
	}
	
	
	def loadProperties() {
			loadPropertiesFromFile("${project.appCodeBase}/version.properties")
			loadPropertiesFromFile("${project.appCodeBase}/build.properties")
			loadPropertiesFromFile("${project.appCodeBase}/config.properties")
			loadPropertiesFromFile("${project.appCodeBase}/db.properties")
			loadPropertiesFromFile("${project.appCodeBase}/common.properties")
			loadPropertiesFromFile("${project.appCodeBase}/examples.properties")
		
		
			project.ext['debuglevel'] = "none"
		
			project.ext['src.dir'] = appCodeBase
			project.ext['src.classes.dir'] = "${project.appCodeBase}/classes"
			project.ext['src.source.dir'] = "${project.appCodeBase}/src"
			project.ext['src.3rdparty.jars'] = "${project.appCodeBase}/3rdparty/lib"
			//project.ext['src.3rdparty.programs'] = '3rdpartycode/programs'
		
			project.ext['out.dir'] = appCodeBase
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
	
}
