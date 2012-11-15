package com.gap.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin
import java.util.ArrayList
import groovy.text.*

class GapJRubyPlugin implements Plugin<Project> {

  void apply(Project project) {
    project.configure(project) {
      configurations {
        jruby
      }
      dependencies {
        jruby 'com.jcraft:jzlib:1.1.1'
        jruby 'org.jruby:jruby-core:1.6.7'
        jruby 'org.jruby:jruby-stdlib:1.6.7'
        jruby 'org.jruby:jruby-common:1.6.7'
      }
    }
  }

}
