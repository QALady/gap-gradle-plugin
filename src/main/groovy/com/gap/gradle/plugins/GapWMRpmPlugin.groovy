package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class GapWMRpmPlugin implements Plugin<Project> {

  @Override
  void apply(Project project) {
    project.ext.UploadRpm = com.gap.gradle.tasks.UploadRpm
  }
}
