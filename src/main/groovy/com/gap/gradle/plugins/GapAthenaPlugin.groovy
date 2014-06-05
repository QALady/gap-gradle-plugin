package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.tasks.InitNAPosBuildTask
import com.gap.gradle.tasks.UploadAthenaBuildRpmToRepo

class GapAthenaPlugin implements Plugin<Project>{
	
	private Project project

	@Override
	public void apply(Project project) {
		this.project = project

		if (project.hasProperty("appCodeBase")) {
			initPOSBuildProperties()
			createSharedManifest()
		}
		
		project.task('uploadBuildRpmToRepo') << {
			new UploadAthenaBuildRpmToRepo(project).execute()
		}
		
		project.task('initNAPosBuild') << {
			new InitNAPosBuildTask(project).execute()
		}

	}

	private void initPOSBuildProperties() {
		project.ext {

            for(file in project.fileTree(dir : project.appCodeBase, include: '*.properties')) {
				loadPropertiesFromFile(file.getAbsolutePath())
			}
			
			project.debuglevel = "none"

			project.src_dir = project.appCodeBase
			project.src_classes_dir = "${project.appCodeBase}/classes"
			project.src_source_dir = "${project.appCodeBase}/src"
			project.src_3rdparty_jars = "${project.appCodeBase}/3rdparty/lib"

			project.out_dir = project.appCodeBase
			project.out_test_dir = "${project.appCodeBase}/test"
			project.out_javadoc_dir = "${project.appCodeBase}/javado"

			project.out_jars_dir = "${project.appCodeBase}/dist"
			project.out_depprojs_dir = "${project.appCodeBase}/dependentProjects"
			project.out_report_dir = "${project.appCodeBase}/reports"

			project.examples_dir = "${project.appCodeBase}/examples"
			project.test_dir = "${project.appCodeBase}/test"
			project.test_classes_dir = "${test_dir}/classes"
			project.deprecation_dir = "${project.appCodeBase}/deprecation"
			project.common_file = "${project.appCodeBase}/common.properties"

			project.src_debug_dir = "${project.appCodeBase}/debug"

			project.jdk_compiler = project.compileJava.targetCompatibility
			
			
			project.third_jarFiles_manifest = ""
			for (file in project.fileTree(dir: project.src_3rdparty_jars, include: '**/*.jar')) {
                String relativePath = file.toString().split(project.appCodeBase)[1]
                project.third_jarFiles_manifest = project.third_jarFiles_manifest + " " + ".."+relativePath
			}
			
		}
	}
	
	private void loadPropertiesFromFile(String sourceFileName) {
		def config = new Properties()
		def propFile = new File(sourceFileName)
		if (propFile.canRead()) {
			config.load(new FileInputStream(propFile))
			for (Map.Entry property in config) {
				project.ext[property.key.replaceAll('\\.', '_')] = property.value
			}
		}
	}
	
	private void createSharedManifest() {

        def manifest_org = new File(project.appCodeBase + "/META-INF/MANIFEST.MF.ORG")
        def manifest_mf = new File(project.appCodeBase + "/META-INF/MANIFEST.MF")

        project.sharedManifest =  project.manifest {

                def attrs = new HashMap<String, String>()

                if (manifest_org.isFile()) {
                    manifest_org.eachLine {
                        if (!it.isEmpty() && it.contains(': ')) {
                            attrs.put(it.split(': ')[0], it.split(': ')[1])
                        }
                    }
                } else if (manifest_mf.isFile()) {

                    manifest_mf.eachLine {
                        if (!it.isEmpty() && it.contains(': ')) {
                            attrs.put(it.split(': ')[0], it.split(': ')[1])
                        }
                    }
                }

                attrs.put('Specification-Version', project.project_shortname)
                attrs.put('Implementation-Version', project.project_buildNumber)
                attrs.put('JDK-Compiler', project.jdk_compiler)

                String sectionName = attrs.get('Name')
                attrs.remove("Name")
                attrs.remove("Manifest-version")
                attrs.remove("Manifest-Version")

                attributes(attrs, sectionName)
            }

            project.sharedManifest.writeTo(project.appCodeBase+"/META-INF/MANIFEST.MF")

    }
	

}
