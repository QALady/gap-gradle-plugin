package com.gap.gradle.plugins

import com.gap.gradle.tasks.UploadFunctionalTestsTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Zip

class GapTestPipelinePlugin implements Plugin<Project> {

    void apply(Project project) {

        project.task('uploadFunctionalTestsTask') << {
            new UploadFunctionalTestsTask(project).execute()
        }
    }


}