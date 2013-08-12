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
        jruby 'org.jruby:jruby-complete:1.7.4'
      }
    }
  }

}
