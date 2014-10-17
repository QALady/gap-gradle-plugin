package com.gap.gradle.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.tasks.CreateHtmlWithGoodVersionsTask
import com.gap.gradle.tasks.GapWMManualPluginTasks
import com.gap.gradle.tasks.UploadGradleWithSelectedDependencyVersionsTask

class GapWMManualPlugin implements Plugin<Project> {

  void apply(Project project) {

  		project.task('createHtmlWithGoodVersions') << {
            new CreateHtmlWithGoodVersionsTask(project).execute()
        }
		  
		project.task('uploadGradleWithSelectedDependencyVersions') << {
			new UploadGradleWithSelectedDependencyVersionsTask(project).execute()
		}
		
		project.task('WMManualCreateLinksForApprovalAndRejection') << {
			new GapWMManualPluginTasks(project).executeCreateLinksForApprovalAndRejection()
		}

		project.task('WMManualRemoveApprovalAndRejectionLinks') << {
			new GapWMManualPluginTasks(project).executeRemoveApprovalAndRejectionLinks()
		}

		project.task('WMManualFailorPassBasedOnProperty') << {
			new GapWMManualPluginTasks(project).executeFailorPassBasedOnProperty()
		}
  }

}