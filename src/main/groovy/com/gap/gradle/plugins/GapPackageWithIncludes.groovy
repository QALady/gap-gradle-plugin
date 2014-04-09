package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.bundling.Zip

/*

To include a dependency: add a dependency using the "include" configuration i.e.:

dependencies{
  include ':dependency-name:1.0.0.1'
}

Note: Gradle will make duplicates of files instead of overwritting them
.. see:
http://issues.gradle.org/browse/GRADLE-2171

*/

class GapPackageWithIncludes implements Plugin<Project>{
  void apply(Project project) {
    project.configurations.create('include') {
      transitive false
      visible false
    };
    project.setBuildDir('build');
    project.tasks.create(
      name:'packageWithIncludes',
      type: Zip,
      description: 'Package (zip) this project with all include dependencies (dependencies are unzipped)')
      {
        baseName project.name
        extension "zip"
        destinationDir project.buildDir
        
        from {project.configurations.include.collect{project.zipTree(it)}}
        from (project.projectDir){
          include '**/*'
          exclude 'build.gradle'
          exclude 'build'
          exclude '.*'
        }
        
         
        inputs.files project.configurations.include
    };

    project.tasks.create(
      name:'clean',
      type: Delete,
      description: 'Clean the working directories for the packageWithIncludes task')
      {
        delete project.buildDir
    };
	}
}
