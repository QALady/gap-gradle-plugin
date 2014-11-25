package com.gap.gradle.plugins

import com.gap.gradle.tasks.CheckDSLFileExistTask
import com.gap.gradle.tasks.GradlelizeFileTask

import javax.inject.Inject

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

import com.gap.gradle.extensions.GapWMSegmentDsl
import com.gap.gradle.tasks.CreateECProcedureTask

class GapWMSegmentDslPlugin implements Plugin<Project> {

	Instantiator instantiator
	Project project
	GapWMSegmentDsl extension
	
	@Inject
	GapWMSegmentDslPlugin(Instantiator instantiator) {
		this.instantiator = instantiator
	}

	@Override
	void apply(Project project) {

		loadSegmentDslConfig(project)

		project.task('createECProcedure') << {
			new CreateECProcedureTask(project).execute()
		}

		project.task("checkDSLFileExist") << {
			new CheckDSLFileExistTask(project).execute()
		}

		project.task("gradlelize") << {
			new GradlelizeFileTask(project).execute()
		}


	}

	def loadSegmentDslConfig(Project project) {
		def segmentExtension = project.extensions.findByName('segment')
		if (!segmentExtension) {
			project.extensions.create("segment", GapWMSegmentDsl, project, instantiator)
		}
	}
}
