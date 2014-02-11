package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.tasks.TriggerProdDeployTask

class GapProdDeployPlugin implements Plugin<Project>{

	@Override
	public void apply(Project project) {
		project.task('triggerProdDeploy') {
			doLast {
				new TriggerProdDeployTask(project).execute()
			}
		}
	}
	
}
