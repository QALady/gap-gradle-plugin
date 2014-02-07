package com.gap.gradle.plugins.git

import com.gap.gradle.utils.ShellCommand
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.gradle.api.Project

/**
 * Created by ccaceres on 2/5/14.
 */
class GitCommitAndPushTask {

    Project project
    Log log = LogFactory.getLog(GitCommitAndPushTask)
    def fullRepo
    def repo
    def gitPath
    def currentPath

    GitCommitAndPushTask(Project project){
        this.project = project
        parametersExist()
        fullRepo = project.gitconfig.fullRepoName
        repo = project.gitconfig.fullRepoName.split("/")[1]      //TODO: Catch invalid repo format
        currentPath = System.getProperty("user.dir")
        gitPath = currentPath + "/" + repo
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
        def userId = project.gitconfig.userId
        ["git", "commit", "-am",
                "'[${userId}] - Commit from Electric Commander'",
                "--author='${userId} <noreply@gap.com>'"].execute(null, new File(gitPath))
    }

    def pushToGit(){
        "git push".execute(null, new File(gitPath))
    }
}
