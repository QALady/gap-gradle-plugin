package com.gap.gradle.plugins.git

import com.gap.gradle.plugins.cookbook.UpdateCookbookSHATask
import org.gradle.api.Plugin
import org.gradle.api.Project

class GapGitPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('gitconfig', GitConfig)

        project.gitconfig.userId = project.hasProperty('userId') ? project.getProperty('userId') : null
        project.gitconfig.fullRepoName = project.hasProperty('fullRepoName') ? project.getProperty('fullRepoName') : null
        project.gitconfig.shaId = project.hasProperty('shaId') ? project.getProperty('shaId') : null

        project.task('promoteCookbookBerksfile') << {
            new UpdateCookbookSHATask(project).execute()
        }
    }
}
