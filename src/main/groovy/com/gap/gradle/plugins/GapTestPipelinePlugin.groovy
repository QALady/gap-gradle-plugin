package com.gap.gradle.plugins

//import com.gap.gradle.tasks.UploadFunctionalTestsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

class GapTestPipelinePlugin implements Plugin<Project> {

    void apply(Project project) {

		project.tasks.add(name:'packageFunctionalTests', type: Zip) {
	        classifier = 'functionTests'
	        from ("${project.projectDir}/functionalTests")
	        include '**/*'
	  	}

	    if (project.plugins.hasPlugin('base')) {
	      	project.configure(project) {
		      	
		        uploadFunctionTests {
		        	//project.configurations.compile.dependencies.each{
		            //	if (it instanceof org.gradle.api.artifacts.ProjectDependency)
		            //  		uploadTests.dependsOn(it.dependencyProject.path + ':uploadTests')
	              	//}
		        	repositories {
		            	ivy {
			            	layout 'maven'
			            	url "http://artifactory.gapinc.dev/artifactory/local-non-prod"
			            	credentials {
			                	username "ec-build-snap"
			                	password "$nap4me"
			              	}
			            }
		    		}
				}

				configurations {
		          functionTests
		        }

		        artifacts {
		          functionTests packageFunctionalTests
		        }
			}
		}
	}
	}