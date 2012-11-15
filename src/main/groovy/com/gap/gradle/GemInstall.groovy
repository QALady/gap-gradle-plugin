package com.gap.gradle

import java.util.Map;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.Project;

class GemInstall extends DefaultTask {

  def String gemName
  def String gemVersion
  def String gemHome
  def String gemSet

  @TaskAction
  def run() {
    def gemDir = new File("${gemHome}/${gemSet}/gems/${gemName}-${gemVersion}")    
    if (!gemDir.isDirectory()) {
      project.javaexec {
        main = 'org.jruby.Main'
        args = ['-S','gem','install',gemName,'-v',gemVersion]
        def os = System.getProperty('os.name').toLowerCase()
        if(!os.contains('windows')){
          environment = [
            GEM_HOME:gemHome + '/' + gemSet,
            GEM_PATH:gemHome + '/' + gemSet,
            PATH:gemHome + '/' + gemSet + 'bin:$PATH',
            http_proxy:'http://qaproxy.gid.gap.com:8080'
          ]  
        }    
        classpath = project.configurations.jruby
      }
    }
  } 
}
