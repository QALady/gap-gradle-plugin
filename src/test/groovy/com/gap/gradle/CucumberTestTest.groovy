package com.gap.gradle

import org.junit.*
import static org.hamcrest.Matchers.*
import static org.hamcrest.core.AnyOf.anyOf
import static org.junit.Assert.*
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.api.Plugin
import org.gradle.api.plugins.JavaPlugin

class CucumberTestTest {

  Project project
  CucumberTest task

  @Before
  public void setup() {
    project = ProjectBuilder.builder().build()
    project.setProperty('collectCoverage', 'false')
    new JavaPlugin().apply(project) 
    new GapJRubyPlugin().apply(project)
    new GapBuildPlugin().apply(project)
    task = project.tasks.add('cucumber', CucumberTest.class)
  }

  @Ignore('Caused by: java.lang.OutOfMemoryError: Java heap space')
  public void taskShouldRun() {
    task.setProperty('gemHome', '/Users/Jimmy/.rvm/gems/ruby-1.9.3-p194')
    task.setProperty('env', [:])
    task.setProperty('testClasspath', project.configurations.getByName('testRuntime'))
    task.run() 
  }

}
