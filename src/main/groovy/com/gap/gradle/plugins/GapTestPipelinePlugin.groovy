package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip
import org.gradle.api.tasks.GradleBuild

class GapTestPipelinePlugin implements Plugin<Project> {

    void apply(Project project) {
    	def artifactoryUrl = "http://artifactory.gapinc.dev/artifactory"
        def folderFunctionalTest = "functionalTests"
        if(project.hasProperty('customFunctionalTestsFolder')){
            folderFunctionalTest = project.customFunctionalTestsFolder
        }
    	project.repositories {
    		ivy {
				name "artifactory"
    			url "${artifactoryUrl}/local-non-prod"
	            credentials {
                    username 'ec-build-snap'
                    password '$nap4me'
                }
    		}
    		maven {
		        url "${artifactoryUrl}/maven-repos"
		    }
		    ivy {
		        layout "maven"
		        url "${artifactoryUrl}/maven-repos"
		    }
    	}

		project.tasks.create(name:'packageFunctionalTests', type: Zip) {
	        appendix = 'functionalTests'
	        classifier = 'tests'
	        from ("${project.projectDir}/${folderFunctionalTest}")
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
        				add project.repositories.artifactory
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
 				project.configurations[config.name].files.each{ file ->
		        	if(file.name ==~ /.*-functionalTests-.*-tests\.zip/ ) {

		         		project.copy {
		         			from project.zipTree(file)
		            		into "${folderFunctionalTest}"
	        			}
		 		 	}
	  			}
      		}
      	}

		project.tasks.create(name: 'executeFunctionalTests', type: GradleBuild, dependsOn: 'downloadFunctionalTests') {
	        buildFile = "${folderFunctionalTest}/build.gradle"
			tasks << 'runFunctionalTests'
	  	}
    }
}