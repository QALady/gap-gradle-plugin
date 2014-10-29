package com.gap.gradle.plugins

import com.gap.gradle.extensions.WatchmenDSLExtension
import com.gap.gradle.tasks.CreateECProcedureTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GapWMSegmentDslPlugin implements Plugin<Project> {

	Project project
	WatchmenDSLExtension extension

	@Override
	void apply(Project project) {

		project.task('createECProcedure') << {
			new CreateECProcedureTask(project).execute()
		}
	}

}
