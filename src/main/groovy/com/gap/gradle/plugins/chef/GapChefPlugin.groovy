package com.gap.gradle.plugins.cookbook

import com.gap.gradle.git.GitConfig
import org.gradle.api.Plugin
import org.gradle.api.Project

class GapChefPlugin implements Plugin<Project> {

    void apply(Project project) {
        project.apply plugin: 'gapcookbook'

        project.extensions.create('gitconfig', GitConfig)

        project.gitconfig.userId = project.hasProperty('userId') ? project.getProperty('userId') : null
        project.gitconfig.fullRepoName = project.hasProperty('fullRepoName') ? project.getProperty('fullRepoName') : null
        project.gitconfig.shaId = project.hasProperty('shaId') ? project.getProperty('shaId') : null

        project.task('promoteCookbookBerksfile') << {
            new UpdateCookbookSHATask(project).execute()
        }
    }
}
