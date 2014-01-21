package com.gap.gradle.plugins

import org.gradle.api.Project
import org.gradle.api.Plugin
import org.gradle.api.tasks.Copy

class GapWarPlugin implements Plugin<Project>{

  void apply(Project project) {
  
    def tomcatHome = project.hasProperty('tomcatHome') ? project.getProperty('tomcatHome') : System.properties['user.home'] + '/tomcat'
    
    if (project.plugins.hasPlugin('war'))
      project.tasks.add(
       
        name:'deployToTomcat', 
        dependsOn: 'war',
        type: Copy,
        description: 'Copy Web Archive (war) to Tomcat directory specified by TOMCAT6_HOME')
        {
          from project.configurations.archives.allArtifacts*.file 
          into "${tomcatHome}/webapps"
        };
    
	}
}
