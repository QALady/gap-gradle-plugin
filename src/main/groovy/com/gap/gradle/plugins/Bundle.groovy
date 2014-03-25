package com.gap.gradle.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class Bundle extends DefaultTask {

  def String gemFile
  def String gemHome
  def String gemSet
  def String customMaxHeapSizeForGem

  @TaskAction
  def run() {
    project.javaexec {
      main = 'org.jruby.Main'
      maxHeapSize = customMaxHeapSizeForGem ?: Runtime.getRuntime().maxMemory()
      args = ['-S','bundle','install','--gemfile',gemFile]      
      def os = System.getProperty('os.name').toLowerCase()
      if(!os.contains('windows')){
        environment = [
          GEM_HOME:gemHome + '/' + gemSet,
          GEM_PATH:gemHome + '/' + gemSet,
          PATH:gemHome + '/' + gemSet + '/bin:$PATH'
        ]
      }
      classpath = project.configurations.jruby
    }
  }

}
