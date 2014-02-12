package com.gap.gradle.plugins

import com.gap.gradle.ProdDeployParameterConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.plugins.cookbook.ChefConfig
import com.gap.gradle.plugins.cookbook.JenkinsConfig
import com.gap.gradle.tasks.DeployToProductionTask
import com.gap.gradle.tasks.PrepareToPromoteToProductionTask
import com.gap.gradle.tasks.PromoteToProductionTask
import com.gap.gradle.utils.ConfigUtil

/**
 *
 * @author krishnarangavajhala
 *
 */
class GapProdDeployPlugin implements Plugin<Project>{

    static def CONFIG_FILE = "${System.getProperty('user.home')}/.watchmen/gapcookbook.properties"

    @Override
	public void apply(Project project) {
        project.extensions.create('prodJenkins', JenkinsConfig)
        project.extensions.create('prodChef', ChefConfig)
        project.extensions.create('prodDeploy', ProdDeployParameterConfig)

        new ConfigUtil().loadConfig(project, CONFIG_FILE)

		project.task('prepareToPromote') {
			doLast {
				new PrepareToPromoteToProductionTask(project).execute()
			}
		}

		project.task('promoteToProduction', dependsOn: 'prepareToPromote') {
			doLast {
				new PromoteToProductionTask(project).execute()
			}
		}

		project.task('deployToProduction', dependsOn: 'promoteToProduction') {
			doLast {
				new DeployToProductionTask(project).execute()
			}
		}
    }
}
