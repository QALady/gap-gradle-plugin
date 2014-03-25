package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

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
