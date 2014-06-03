package com.gap.gradle.plugins

import com.gap.gradle.tasks.JarNAPosBuildTask
import org.gradle.api.Plugin
import org.gradle.api.Project

import com.gap.gradle.tasks.InitNAPosBuildTask
import com.gap.gradle.tasks.UploadAthenaBuildRpmToRepo

class GapAthenaPlugin implements Plugin<Project>{

	@Override
	public void apply(Project project) {
		project.task('uploadBuildRpmToRepo') << {
			new UploadAthenaBuildRpmToRepo(project).execute()
		}

		project.task('initNAPosBuild') << {
			new InitNAPosBuildTask(project).execute()
		}

        project.task('jarNAPosBuild') << {
            new JarNAPosBuildTask(project).execute()
        }
	}

}
