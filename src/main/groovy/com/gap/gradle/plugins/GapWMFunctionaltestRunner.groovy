package com.gap.gradle.plugins

import com.gap.gradle.tasks.WatchmenUploadFunctionalTestsTask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GapWMFunctionaltestRunner implements Plugin<Project> {

    void apply(Project project){
        project.task('watchmenUploadFunctionalTests') << {
            new WatchmenUploadFunctionalTestsTask(project).execute()
        }
    }
}
