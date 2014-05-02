package com.gap.gradle.plugins

import org.apache.tools.ant.types.Path
import org.apache.tools.ant.types.Path.PathElement
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.extensions.GapAntHelperConfig

class GapAntHelperPlugin implements Plugin<Project>{

	@Override
	public void apply(Project project) {

		println project.antPathName
		println project.antDepConf
		PathElement pathElement
		Path antPath = project.ant.getReferences().get(project.antPathName)
		project.configurations[project.antDepConf].each { File f ->
			pathElement = antPath.createPathElement()
			pathElement.setLocation(f)
			pathElement.setPath(f.absolutePath)
		}

		
	}

}
