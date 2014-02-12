package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.plugins.cookbook.ChefConfig
import com.gap.gradle.plugins.cookbook.JenkinsConfig
import com.gap.gradle.tasks.DeployToProductionTask
import com.gap.gradle.utils.ConfigUtil

class GapProdDeployPlugin implements Plugin<Project>{

    static def CONFIG_FILE = "${System.getProperty('user.home')}/.watchmen/gapcookbook.properties"

    @Override
	public void apply(Project project) {
        project.extensions.create('jenkins', JenkinsConfig)
        project.extensions.create('chef', ChefConfig)

        new ConfigUtil().loadConfig(project, CONFIG_FILE)

		project.task('deployToProduction') {
			doLast {
				new DeployToProductionTask(project).execute()				
			}
		}
    }
}
