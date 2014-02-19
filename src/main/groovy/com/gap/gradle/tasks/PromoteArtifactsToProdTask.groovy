package com.gap.gradle.tasks

import com.gap.gradle.utils.GradleTask

class PromoteArtifactsToProdTask {

    private def project
    private def gradleTask

    PromoteArtifactsToProdTask (project,gradleTask = new GradleTask()){
        this.gradleTask = gradleTask
        this.project = project
    }

    def execute(){
        gradleTask.execute(project,'downloadArtifacts')
    }
}
