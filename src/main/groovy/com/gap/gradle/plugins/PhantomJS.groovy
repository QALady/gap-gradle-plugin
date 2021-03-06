package com.gap.gradle.plugins

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/*
 * Upload new versions of phantomjs to artifactory at:
 * local-external:org/phantomjs/phantomjs/{version}/phantomjs-{version}-{qualifier}.zip
 *
 * Contents of zip should be ONLY the single binary executable for phantomjs
 *
 * EXAMPLE in build.gradle:
 * task(type: com.gap.gradle.plugins.PhantomJS){
 *     version: '1.9.1'
 *     args = [ 'runner.js', 'jasmine-runner.html' ]
 * }
 */
class PhantomJS extends DefaultTask {
  def Iterable<?> args
  def String version = '+'
  def final String phantomjsBin    = "${project.buildDir}/bin/phantomjs"
  def final String phantomjsBinExe = "${project.buildDir}/bin/phantomjs.exe"

  @TaskAction
  def run() {
    def final String os = System.properties['os.name'].toLowerCase()
    def final String arch = System.properties['os.arch'].toLowerCase()

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
        executable = qualifier == 'windows' ? phantomjsBinExe : phantomjsBin
        args = this.args
    }
  }
}
