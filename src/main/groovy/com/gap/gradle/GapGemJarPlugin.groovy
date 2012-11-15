package com.gap.gradle

import org.gradle.api.Project
import org.gradle.api.Plugin

class GapGemJarPlugin implements Plugin<Project> {

  void apply(Project target) {
    target.configure(target) {
      repositories {
        ivy {
          url 'http://ci.gap.dev/gemjar'
          layout 'pattern', {
            artifact 'jars/[organization]/[module]-[revision].[ext]'
            ivy 'ivys/[organization]/ivy-[module]-[revision].[ext]'
          }   
        }
      }
    }
  }

}
