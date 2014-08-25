package com.gap.gradle.plugins

//import com.gap.gradle.tasks.UploadFunctionalTestsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.Copy
import java.util.regex.Matcher
import java.util.regex.Pattern

class GapTestPipelinePlugin implements Plugin<Project> {

    void apply(Project project) {

		project.tasks.add(name:'packageFunctionalTests', type: Zip) {
	        appendix = 'functionalTests'
	        classifier = 'tests'
	        from ("${project.projectDir}/functionalTests")
	        include '**/*'
	  	}

	  	/*
	  	project.tasks.add(name:'packageIntegrationTests', type: Zip) {
	        appendix = 'integrationTests'
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
		
		//Downloads functionalTests archive by looking at all configuraitons and unzips to functionalTests dir
 		project.task('downloadFunctionalTests') << {
 			project.configurations.each { config ->
 				configurations[config.name].files.each{ file ->
		         if(file.name ==~ /.*-functionalTests-.*-tests\.zip/ ) {
		         	copy {
		         		from zipTree(file.path)
		            	into 'functionalTests'
	        		}
		 		 }
	  		}
      	}
    }
}