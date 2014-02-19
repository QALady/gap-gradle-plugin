package com.gap.gradle.utils

import org.gradle.api.invocation.Gradle

class GradleTask {
    def execute(project, taskName){
        project.tasks.findByName(taskName).execute()
    }
}
