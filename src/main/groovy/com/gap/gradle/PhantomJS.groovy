package com.gap.gradle

import java.util.Map;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.Project;

class PhantomJS extends DefaultTask {
  def args
  def version
  def final String phantomjsBin = "${buildDir}/bin"

  @TaskAction
  def run() {
    if (os.indexOf('linux') >= 0) {
        if (arch.indexOf('64') >= 0) {
            qualifier "linux-x86_64"
        } else {
            qualifier "linux-i686"
        }
    } else if (os.indexOf('mac') >= 0) {
        qualifier "macosx"
    } else {
        qualifier "windows"
    }

    project.configurations { binaries }
    project.dependencies.binaries "org.phantomjs:phantomjs:${version}:${qualifier}@zip"
    project.configurations.binaries.each { file ->
        copy {
            from zipTree(file)
            into "${buildDir}/bin"
        }
    }

    project.exec {
        executable = phantomjsBin
        args = args
    }
  }
}
