package com.gap.gradle.plugins

//import com.gap.gradle.tasks.UploadFunctionalTestsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

class GapTestPipelinePlugin implements Plugin<Project> {

    void apply(Project project) {

		project.tasks.add(name:'packageFunctionalTests', type: Zip) {
	        classifier = 'functionTests'
	        from ("${project.projectDir}/functional-tests")
	        include '**/*'
	  	}

	    if (project.plugins.hasPlugin('java')) {
	      	project.configure(project) {
                configurations {
                    functionTests
                }
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
			                	username "ec-build"
			                	password "EC-art!"
			              	}
			            }
		    		}
				}



		        artifacts {
		          functionTests packageFunctionalTests
		        }
			}
		}
	}
	}