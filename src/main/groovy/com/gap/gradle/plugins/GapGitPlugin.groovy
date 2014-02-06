package com.gap.gradle.plugins

import com.gap.gradle.plugins.git.GitCommitAndPushTask
import com.gap.gradle.plugins.git.GitCommitAndPushTask
import com.gap.gradle.plugins.git.GitConfig
import com.gap.gradle.plugins.git.GitCheckoutTask
import com.gap.gradle.plugins.git.GitUpdateSHATask
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by ccaceres on 2/5/14.
 */
class GapGitPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.extensions.create('gitconfig', GitConfig)
        project.task('gitCheckout') << {
            new GitCheckoutTask(project).execute()
        }
        project.task('gitUpdateSHA', dependsOn: 'gitCheckout') << {
            new GitUpdateSHATask(project).execute()
        }
        project.task('gitCommitAndPush') << {
            new GitCommitAndPushTask(project).execute()
        }
    }
}
