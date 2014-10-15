package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.tasks.CreateHtmlWithGoodVersionsTask
import com.gap.gradle.tasks.UploadGradleWithSelectedDependencyVersionsTask

class GapWMManualPlugin implements Plugin<Project> {

  void apply(Project project) {

  		project.task('createHtmlWithGoodVersions') << {
            new CreateHtmlWithGoodVersionsTask().execute()
        }
		  
		project.task('uploadGradleWithSelectedDependencyVersions') << {
			new UploadGradleWithSelectedDependencyVersionsTask(project).execute()
		}
  }
}

