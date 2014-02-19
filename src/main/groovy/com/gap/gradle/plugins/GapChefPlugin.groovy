package com.gap.gradle.plugins

import com.gap.pipeline.GitConfig
import com.gap.gradle.tasks.UpdateCookbookSHATask;

import org.gradle.api.Plugin
import org.gradle.api.Project

class GapChefPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.apply plugin: 'gapcookbook'

        project.extensions.create('git', GitConfig)

        project.git.userId = project.hasProperty('userId') ? project.getProperty('userId') : null
        project.git.fullRepoName = project.hasProperty('fullRepoName') ? project.getProperty('fullRepoName') : null
        project.git.sha1Id = project.hasProperty('sha1Id') ? project.getProperty('sha1Id') : null

        project.task('promoteCookbookBerksfile') << {
            new UpdateCookbookSHATask(project).execute()
        }
    }
}
