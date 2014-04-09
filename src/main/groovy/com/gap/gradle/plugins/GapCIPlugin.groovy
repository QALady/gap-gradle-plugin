package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class GapCIPlugin implements Plugin<Project> {

  @Override
  void apply(Project target) {
  
    def runSonarMetrics = false
  
    target.configure(target) {

      //metrics task
      target.tasks.create(name: 'metrics',
          description: 'Collects project metrics.',
          group: 'Continuous Integration')
      
      if(project.hasProperty('runSonarMetrics'))
        runSonarMetrics = project.getProperty('runSonarMetrics')
      
      if(runSonarMetrics)
        target.tasks['metrics'].dependsOn(target.getTasksByName('sonarAnalyze', true))      
      
      //precommit task
      target.tasks.create(name: 'precommit',
          description: 'Recommended for execution before pushing changes to SCM.',
          group: 'Continuous Integration',
          dependsOn: [target.getTasksByName('test', true), target.getTasksByName('metrics', true)])
      
      //commit stage task
      target.tasks.create(name: 'commit-stage',
          description: 'Recommended for execution before pushing changes to SCM.',
          group: 'Continuous Integration',
          dependsOn: [target.getTasksByName('test', true), target.getTasksByName('metrics', true), target.getTasksByName('uploadArchives', true)])
    }
  }
}
