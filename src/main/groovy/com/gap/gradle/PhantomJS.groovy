package com.gap.gradle

import java.util.Map;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Exec;
import org.gradle.api.Project;

/*
 * Upload new versions of phantomjs to artifactory at:
 * ext-release-local/org/phantomjs/phantomjs/{version}/phantomjs-{version}-{qualifier}.zip
 *
 * Contents of zip should be ONLY the single binary executable for phantomjs
 *
 * EXAMPLE in build.gradle:
 * task(type: com.gap.gradle.PhantomJS){
 *     version: '1.9.1'
 *     args = [ 'runner.js', 'jasmine-runner.html' ]
 * }
 */
class PhantomJS extends DefaultTask {
  def Iterable<?> args
  def String version = '1.9.1'
  def final String phantomjsBin = "${project.buildDir}/bin/phantomjs"

  @TaskAction
  def run() {
    def String os = System.properties['os.name'].toLowerCase()
    def String arch = System.properties['os.arch'].toLowerCase()

    def String qualifier
    if (os.indexOf('linux') >= 0) {
        if (arch.indexOf('64') >= 0) {
            qualifier = "linux-x86_64"
        } else {
            qualifier = "linux-i686"
        }
    } else if (os.indexOf('mac') >= 0) {
        qualifier = "macosx"
    } else {
        qualifier = "windows"
    }

    project.configurations { binaries }
    project.dependencies.binaries group: "org.phantomjs", name:"phantomjs", version: "${version}", classifier: "${qualifier}", ext: "zip"
    project.configurations.binaries.each { file ->
        project.copy {
            from project.zipTree(file)
            into "${project.buildDir}/bin"
        }
    }

    project.exec {
        executable = phantomjsBin
        args = this.args
    }
  }
}
