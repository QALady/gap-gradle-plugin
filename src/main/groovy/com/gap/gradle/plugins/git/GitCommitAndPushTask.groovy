package com.gap.gradle.plugins.git

import com.gap.gradle.utils.ShellCommand
import org.gradle.api.Project

/**
 * Created by ccaceres on 2/5/14.
 */
class GitCommitAndPushTask {

    Project project

    GitCommitAndPushTask(Project project){
        this.project = project
        parametersExist()
    }

    def parametersExist(){
        if(project.gitconfig.fullRepoName == null){
            throw new Exception('There is no fullRepoName defined')
        }
        if(project.gitconfig.userId == null){
            throw new Exception('There is no user id defined')
        }
    }

    def execute(){
        commitToGit()
        pushToGit()
    }

    def commitToGit(){
        def repo = project.gitconfig.fullRepoName.split("/")[1]
        def userId = project.gitconfig.userId
        new ShellCommand().execute("cd " + repo)
        new ShellCommand().execute("git commit -am'[" + userId
                + "] - Commit from Electric Commander' --author='"+ userId
                + " <" + userId + "@gap.com>'")
    }

    def pushToGit(){
        new ShellCommand().execute('git push')
    }
}
