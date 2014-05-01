package com.gap.gradle.tasks

import org.apache.tools.ant.types.Path
import org.apache.tools.ant.types.Path.PathElement
import org.gradle.api.Project

import com.gap.pipeline.tasks.WatchmenTask
import com.gap.pipeline.tasks.annotations.Require
import com.gap.pipeline.tasks.annotations.RequiredParameters

@RequiredParameters([
	@Require(parameter = 'antHelperConfig.antPathName', description = "The Ant Path element id to import the gradle dependencies."),
	@Require(parameter = 'antHelperConfig.dependencyConfigurationName', description = "The gradle configuration listing all the dependencies to be imported into Ant path element.")
])

class AddDepsToPathTask extends WatchmenTask {

		Project project
	
		AddDepsToPathTask(Project project) {
			super(project)
			this.project = project
		}
	
		def execute() {
			PathElement pathElement
			Path antPath = project.ant.getReferences().get(project.antHelperConfig.antPathName)
			project.configurations[project.antHelperConfig.dependencyConfigurationName].each { File f ->
				pathElement = antPath.createPathElement()
				pathElement.setLocation(f)
				pathElement.setPath(f.absolutePath)
			}
		}
}