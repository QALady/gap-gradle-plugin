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
		configJavac()
		createDirectories()
		addCustomPathsToSourceSets()
		addCustomCompileDependencies()
	}

	private addCustomCompileDependencies() {
		project.dependencies {
			compile project.files(getClassPath("${project.appCodeBase}/depends.properties"))
		}
	}

	private addCustomPathsToSourceSets() {
		project.sourceSets {
			main {
				output.classesDir = project.src_classes_dir

				java {
					srcDir project.src_source_dir
				}
			}

			test {
				output.classesDir = project.test_classes_dir

				java {
					srcDirs project.test_dir + '/src'
				}
			}
		}
	}
	
	def getClassPath(dependsPropertiesFile) {
		
		List jars = new ArrayList<File>()
	
		jars.add(project.src_3rdparty_jars + "/ingenctl.jar")
	
		//read the depends from depends.properties
		def config = new Properties()
		def propFile = new File(dependsPropertiesFile)
		if (propFile.canRead()) {
			config.load(new FileInputStream(propFile))
	
			for(file in project.fileTree(dir : project.src_dir+'/'+config['classpath.addJar'], include: '*.jar')) {
				jars.add(file)
			}
	
			for (Map.Entry property in config) {
				for(file in project.fileTree(dir : project.src_dir+'/'+property.value+'/dist', include: '*.jar', exclude: ['*source.jar'])) {
					jars.add(file)
				}
			}
		}
	
		//read the depends from ProductMigration/3rdparty/lib
		for(file in project.fileTree(dir : project.src_3rdparty_jars, include: ['**/*.jar','**/*.zip'])) {
			jars.add(file)
		}
	
		return jars
	}
		

	def createDirectories() {
			project.classesDir = new File(project.src_classes_dir)
			project.testDir = new File(project.test_classes_dir)
			project.deprecation = new File(project.deprecation_dir + '/classes')
			project.distDir = new File(project.out_jars_dir)
		
			project.classesDir.mkdirs()
			project.testDir.mkdirs()
			project.deprecation.mkdirs()

			project.delete(project.out_javadoc_dir, project.out_jars_dir)

			project.distDir.mkdirs()			
	}
		
	def configJavac() {
		[project.compileJava, project.compileTestJava]*.options.collect {options -> options.encoding = 'ISO-8859-1'}
		//[compileJava, compileTestJava]*.options.collect {options -> options.debuglevel = project.properties['debuglevel']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.debug = project.properties['debug']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.deprecation = project.properties['deprecation']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.optimize = project.properties['optimize']}
		//[compileJava, compileTestJava]*.options.collect {options -> options.source = project.properties['project.source.flag']}
	}
	
	
}
