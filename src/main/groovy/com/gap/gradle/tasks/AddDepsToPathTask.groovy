package com.gap.gradle.tasks

import org.apache.tools.ant.types.Path
import org.apache.tools.ant.types.Path.PathElement;
import org.gradle.api.Project

class AddDepsToPathTask {

		Project project
	
		AddDepsToPathTask(Project project) {
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