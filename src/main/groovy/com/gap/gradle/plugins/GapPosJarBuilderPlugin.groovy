package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.bundling.ZipEntryCompression

class GapPosJarBuilderPlugin implements Plugin<Project> {

	private Project project

	@Override
	public void apply(Project project) {
		this.project = project
		//jar creation tasks
		
		project.task('mainJar', type: Jar) {
			
			destinationDir = project.file(project.out_jars_dir)
			//archiveName = project.project_jar_main_name
			baseName = project.project_brand + project.project_fullname
			appendix = "main"
			version = project.project_version
			classifier = "devbuild"

			from(project.src_classes_dir){
				include "**/*.class"
				exclude "**/shopx/**/*.*"
			}
			manifest {
				def attrs = new HashMap<String, String>()
				attrs.put('Class-Path',project.third_jarFiles_manifest)
				manifest.attributes(attrs)
				from project.sharedManifest
			}
		}
		
		project.task('sourceJar', type: Jar) {

			destinationDir = project.file(project.out_jars_dir)
			//archiveName = project.project_jar_src_name
			baseName = project.project_brand + project.project_fullname
			appendix = "source"
			version = project.project_version
			classifier = "devbuild"

			
			from(project.src_source_dir) {
				include "**/*.class"
				exclude "**/shopx/**/*.*"
			}
			
			manifest {
				from project.sharedManifest
			}
		}
		
		project.task('resourceJar', type: Jar) {
			
			destinationDir = project.file(project.out_jars_dir)
			//archiveName = project.project_jar_res_name
			baseName = project.project_brand + project.project_fullname
			appendix = "resource"
			version = project.project_version
			classifier = "devbuild"

			
			from(project.src_source_dir) {
				exclude('**/*.java')
				exclude('**/*.class')
			}
			
			manifest {
				from project.sharedManifest
			}
			
			entryCompression  ZipEntryCompression.STORED
		}
		
		project.task('configJar', type: Jar) {
			
			destinationDir = project.file(project.out_jars_dir)
			//archiveName = project.project_jar_config_name
			baseName = project.project_brand + project.project_fullname
			appendix = "config"
			version = project.project_version
			classifier = "devbuild"

			
			from(project.src_dir){
				include  ('config/**')
				include  ('config/*')
			}
			
			manifest {
				from project.sharedManifest
			}
		}
		
		project.task('testMainJar', type: Jar) {
			
			destinationDir = project.file(project.out_jars_dir)
			//archiveName = project.project_jar_test_name
			baseName = project.project_brand + project.project_fullname
			appendix = "testMain"
			version = project.project_version
			classifier = "devbuild"

			
			from(project.test_dir+'/classes'){
				include "**/*.class"
			}
			
			manifest {
				from project.sharedManifest
			}
		}
		
		project.task('testSrcJar', type: Jar) {
			
			destinationDir = project.file(project.out_jars_dir)
			//archiveName = project.project_jar_test_src_name
			baseName = project.project_brand + project.project_fullname
			appendix = "testSrc"
			version = project.project_version
			classifier = "devbuild"

			
			from(project.test_dir+'/src'){
				include "**/*.java"
			}
			
			manifest {
				from project.sharedManifest
			}
		}
		
		project.task('testResJar', type: Jar) {
			
			destinationDir = project.file(project.out_jars_dir)
			//archiveName = project.project_jar_test_res_name
			baseName = project.project_brand + project.project_fullname
			appendix = "testRes"
			version = project.project_version
			classifier = "devbuild"

			
			from(project.test_dir+'/src'){
				exclude('**/*.java')
				exclude('**/*.class')
			}
			
			manifest {
				from project.sharedManifest
			}
		}

	}

}
