package com.gap.gradle.plugins

import com.gap.gradle.tasks.AddDepsToPathTask

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.extensions.GapAntHelperConfig;

class GapAntHelperPlugin implements Plugin<Project>{

	@Override
	public void apply(Project project) {

		project.extensions.create('antHelperConfig', GapAntHelperConfig)

		project.task('addDependencyConfigToAntPath') << {
            new AddDepsToPathTask(project).execute()
        }
		
	}

}
