package com.gap.gradle.plugins

import org.apache.tools.ant.types.Path
import org.apache.tools.ant.types.Path.PathElement
import org.gradle.api.Plugin
import org.gradle.api.Project

import org.apache.commons.logging.LogFactory

class GapAntHelperPlugin implements Plugin<Project>{

    private static final logger = LogFactory.getLog(GapAntHelperPlugin)

	@Override
	public void apply(Project project) {

        logger.info(project.antPathName)
        logger.info(project.antDepConf)
		PathElement pathElement
		Path antPath = project.ant.getReferences().get(project.antPathName)
		if (antPath) {
			project.configurations[project.antDepConf].each { File f ->
				pathElement = antPath.createPathElement()
				pathElement.setLocation(f)
				pathElement.setPath(f.absolutePath)
			}
		}
	}

}
