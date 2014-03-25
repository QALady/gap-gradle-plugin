package com.gap.gradle.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class GemInstall extends DefaultTask {

  def String gemName
  def String gemVersion
  def String gemHome
  def String gemSet
  def String customMaxHeapSizeForGem

  @TaskAction
  def run() {
    def gemDir = new File("${gemHome}/${gemSet}/gems/${gemName}-${gemVersion}")    
    if (!gemDir.isDirectory()) {
      project.javaexec {
        main = 'org.jruby.Main'
        maxHeapSize = customMaxHeapSizeForGem ?: Runtime.getRuntime().maxMemory()
        args = ['-S','gem','install',gemName,'-v',gemVersion,'--clear-sources','--source','http://ks64.phx.gapinc.dev/gemrepo/']
        def os = System.getProperty('os.name').toLowerCase()
        if(!os.contains('windows')){
          environment = [
            GEM_HOME:gemHome + '/' + gemSet,
            GEM_PATH:gemHome + '/' + gemSet,
            PATH:gemHome + '/' + gemSet + 'bin:$PATH'
          ]  
        }    
        classpath = project.configurations.jruby
      }
    }
  } 
}
