package com.gap.gradle.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

class CucumberTest extends DefaultTask {

  def String gemHome
  def String gemSet
  def String userDir
  def testClasspath
  def Map env
  def String feature
  def List vmArgs = []

  @TaskAction
  def run() {
    project.javaexec {
      main = 'org.jruby.Main'
      systemProperty 'user.dir', project.projectDir.toString() + userDir
      jvmArgs = vmArgs
      if (gemHome != null) {
        args = ['-S','cucumber',feature.split()].flatten()
        def os = System.getProperty('os.name').toLowerCase()
        if(!os.contains('windows')){
          environment = [
            GEM_HOME:gemHome + '/' + gemSet,
            GEM_PATH:gemHome + '/' + gemSet,
            PATH:gemHome + '/' + gemSet + '/bin:$PATH'
          ]
          if (env != null) environment.putAll(env)
        }
      } else {
        args = ['classpath:bin/cucumber', feature.split()].flatten()
        environment = env
      }
      if (project.collectCoverage == 'true') {
        systemProperty 'net.sourceforge.cobertura.datafile', project.tasks.coberturaPrepare.datafileLocation
        classpath = project.files(project.tasks.coberturaInstrument.outputDir) +
                    testClasspath +
                    project.configurations.coberturaRuntime +
                    project.configurations.jruby
      } else {
        classpath = testClasspath + project.configurations.jruby
      }
    }
  }

}