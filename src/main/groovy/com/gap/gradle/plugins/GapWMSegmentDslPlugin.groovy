package com.gap.gradle.plugins

import javax.inject.Inject

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator

import com.gap.gradle.extensions.GapWMSegmentDsl
import com.gap.gradle.tasks.CheckDSLFileExistTask
import com.gap.gradle.tasks.ConfigureDefaultResourceFromDsl
import com.gap.gradle.tasks.CreateECProcedureTask
import com.gap.gradle.tasks.GenerateGradleWMSegmentDslFromPropertiesTask
import com.gap.gradle.tasks.PostWMSegmentPhasesTask

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

		project.task('configureDefaultResource') << {
			new ConfigureDefaultResourceFromDsl(project).execute()
		}

		project.task('createECProcedure') << {
			new CreateECProcedureTask(project).execute()
		}

		project.task('postWMSegmentPhases') << {
			new PostWMSegmentPhasesTask(project).execute()
		}

		project.task("checkDSLFileExist") << {
			new CheckDSLFileExistTask(project).execute()
		}

		project.task("generateGradleWMSegmentDslFromProperties") << {
			new GenerateGradleWMSegmentDslFromPropertiesTask(project).execute()
		}

	}

	def loadSegmentDslConfig(Project project) {
		def segmentExtension = project.extensions.findByName('segment')
		if (!segmentExtension) {
			project.extensions.create("segment", GapWMSegmentDsl, project, instantiator)
		}
	}
}
