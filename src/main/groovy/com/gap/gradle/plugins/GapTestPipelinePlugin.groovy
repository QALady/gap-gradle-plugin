package com.gap.gradle.plugins

//import com.gap.gradle.tasks.UploadFunctionalTestsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.Copy

class GapTestPipelinePlugin implements Plugin<Project> {

    void apply(Project project) {

		project.tasks.add(name:'packageFunctionalTests', type: Zip) {
	        classifier = 'tests'
	        from ("${project.projectDir}/functionalTests")
	        include '**/*'
	  	}

	  	/*
	  	project.tasks.add(name:'packageIntegrationTests', type: Zip) {
	        classifier = 'tests'
	        from ("${project.projectDir}/integrationTests")
	        include '*'
	  	} 
	  	*/

	    if (project.plugins.hasPlugin('base')) {
	      	project.configure(project) {
                configurations {
                    testArchives
                }
		        uploadTestArchives {
		        	repositories {
		            	ivy {
			            	layout 'maven'
			            	url "http://artifactory.gapinc.dev/artifactory/local-non-prod"
			            	credentials {
			                	username "ec-build"
			                	password "EC-art!"
			              	}
			            }
		    		}
				}

		        artifacts {
		          testArchives packageFunctionalTests
		          //testArchives packageIntegrationTests
		        }
			}
		}
		
		
 		project.tasks.add(name:'downloadFunctionalTests', type: Copy) {
	        from project.configurations.each { config ->
	        	config.filter{ it.name.contains('functionTests')}
	        }
	        into 'functionalTests'
	  	}
	}
}