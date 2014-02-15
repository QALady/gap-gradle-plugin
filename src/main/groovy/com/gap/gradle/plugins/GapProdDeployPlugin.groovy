package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.ProdDeployParameterConfig
import com.gap.gradle.ProdPrepareConfig;
import com.gap.gradle.plugins.cookbook.JenkinsConfig
import com.gap.gradle.tasks.DeployToProductionTask
import com.gap.gradle.tasks.PrepareToPromoteToProductionTask
import com.gap.gradle.tasks.PromoteToProductionTask
import com.gap.gradle.utils.ConfigUtil

class GapProdDeployPlugin implements Plugin<Project>{

    static def CONFIG_FILE = "${System.getProperty('user.home')}/.watchmen/gapcookbook.properties"

    @Override
	public void apply(Project project) {
		loadJenkinsConfig(project)
		loadProdDeployConfig(project)

		project.task('prepareToPromote') << {
			println "preparing to promote"
			new PrepareToPromoteToProductionTask(project).execute()
		}

		project.task('promoteToProduction', dependsOn: 'prepareToPromote') << {
			println "promoting to production"
			new PromoteToProductionTask(project).execute()
		}

		project.task('deployToProduction', dependsOn: 'promoteToProduction') << {
			println "deploying to production"
			new DeployToProductionTask(project).execute()
			println "WOO! done (:"
		}
    }
	
	private void loadJenkinsConfig(Project project) {
		def jenkinsExtension = project.extensions.findByName('jenkins')
		if (!jenkinsExtension) {
			project.extensions.create('jenkins', JenkinsConfig)
			new ConfigUtil().loadConfig(project, CONFIG_FILE)
		}
	}
	
	private void loadProdDeployConfig(Project project) {
		def prodDeploy = project.extensions.findByName("prodDeploy")
		if (!prodDeploy && project.hasProperty('paramJsonPath')) {
			project.extensions.create('prodDeploy', ProdDeployParameterConfig, new ConfigUtil().loadConfigFromJson(project.paramJsonPath + ProdPrepareConfig.FILE_NAME))
		} else {
			project.extensions.create('prodDeploy', ProdDeployParameterConfig)
		}
	}
}
